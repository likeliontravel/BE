package org.example.be.external.tourapi.util;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.example.be.external.tourapi.dto.AreaDTO;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JsonProcessingException;

// TourApiParser 단위 테스트.
// '정상 0건' 은 빈 리스트로, '해석할 수 없는 응답' 은 BusinessException(EXTERNAL_API_FAILED) 으로 구분되는지 검증한다.
// 예외로 던져져야 Reader 가 수집 실패로 집계하고 TourDataJobListener 의 실패율 판정에 잡힌다(Task 2-9).
@DisplayName("TourApiParser 정상 0건과 파싱 실패 구분 단위 테스트")
class TourApiParserTest {

	private final TourApiParser tourApiParser = new TourApiParser();

	@Test
	@DisplayName("item 이 배열이면 원소를 그대로 반환한다")
	void itemArray_returnsAllItems() {
		String json = """
			{"response":{"header":{"resultCode":"0000","resultMsg":"OK"},
			 "body":{"items":{"item":[{"contentid":"A"},{"contentid":"B"}]},"totalCount":2}}}
			""";

		List<Map<String, Object>> items = tourApiParser.parseItems(json);

		assertThat(items).extracting(item -> item.get("contentid")).containsExactly("A", "B");
	}

	@Test
	@DisplayName("item 이 단일 객체면 1건 리스트로 감싼다")
	void singleItemObject_returnsSingletonList() {
		String json = """
			{"response":{"header":{"resultCode":"0000","resultMsg":"OK"},
			 "body":{"items":{"item":{"contentid":"A"}},"totalCount":1}}}
			""";

		List<Map<String, Object>> items = tourApiParser.parseItems(json);

		assertThat(items).hasSize(1);
		assertThat(items.get(0)).containsEntry("contentid", "A");
	}

	@Test
	@DisplayName("결과가 없어 items 가 빈 문자열이면 정상 0건이다 (2026-10-03 실측 응답)")
	void emptyStringItems_isNormalEmptyResult() {
		// areaCode 17 x contentTypeId 38 관찰 실행에서 받은 응답 원문
		String json = """
			{"response": {"header":{"resultCode":"0000","resultMsg":"OK"},"body": {"items": "","numOfRows":0,"pageNo":1,"totalCount":0}}}
			""";

		assertThat(tourApiParser.parseItems(json)).isEmpty();
	}

	@Test
	@DisplayName("items 또는 item 이 없으면 정상 0건이다")
	void missingItemsOrItem_isNormalEmptyResult() {
		String noItems = """
			{"response":{"body":{"totalCount":0}}}
			""";
		String noItem = """
			{"response":{"body":{"items":{},"totalCount":0}}}
			""";

		assertThat(tourApiParser.parseItems(noItems)).isEmpty();
		assertThat(tourApiParser.parseItems(noItem)).isEmpty();
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@ValueSource(strings = {
		"{\"response\":{\"body\":",                                                     // 잘린 JSON
		"<OpenAPI_ServiceResponse><cmmMsgHeader><errMsg>SERVICE ERROR</errMsg></cmmMsgHeader></OpenAPI_ServiceResponse>",
		// 게이트웨이 XM
		"[]",                                                                            // 최상위가 객체가 아님
		""                                                                               // 빈 본문
	})
	@DisplayName("JSON 객체로 읽을 수 없으면 원인 예외를 보존한 채 EXTERNAL_API_FAILED 로 던진다")
	void unreadableJson_throwsWithCause(String json) {
		assertThatThrownBy(() -> tourApiParser.parseItems(json))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("TourAPI 응답 파싱 실패")
			.hasCauseInstanceOf(JsonProcessingException.class)
			.satisfies(e -> assertThat(((BusinessException)e).getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_FAILED));
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@ValueSource(strings = {
		"null",                                                                          // readValue 가 null 을 반환
		"{\"responseTime\":\"2026-10-03\",\"resultCode\":\"10\",\"resultMsg\":\"INVALID_REQUEST_PARAMETER_ERROR\"}",
		// response 없는 오류
		"{\"response\":{\"header\":{\"resultCode\":\"0000\"}}}",                         // body 없음
		"{\"response\":{\"body\":{\"items\":42}}}",                                      // items 가 객체도 빈 문자열도 아님
		"{\"response\":{\"body\":{\"items\":\"unexpected\"}}}",                          // 비어 있지 않은 문자열
		"{\"response\":{\"body\":{\"items\":{\"item\":\"text\"}}}}"                       // item 이 배열도 객체도 아님
	})
	@DisplayName("JSON 이지만 기대한 구조가 아니면 EXTERNAL_API_FAILED 로 던진다")
	void unexpectedStructure_throws(String json) {
		assertThatThrownBy(() -> tourApiParser.parseItems(json))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("TourAPI 응답 파싱 실패")
			.satisfies(e -> assertThat(((BusinessException)e).getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_FAILED));
	}

	@Test
	@DisplayName("응답 본문이 null 이면 EXTERNAL_API_FAILED 로 던진다")
	void nullBody_throws() {
		assertThatThrownBy(() -> tourApiParser.parseItems(null))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> assertThat(((BusinessException)e).getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_FAILED));
	}

	@Test
	@DisplayName("상위 파서(parseAreas) 도 파싱 실패를 빈 결과로 바꾸지 않고 전파한다")
	void parseAreas_propagatesFailure() {
		assertThatThrownBy(() -> tourApiParser.parseAreas("<html>Bad Gateway</html>"))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	@DisplayName("상위 파서(parseAreas) 는 정상 0건이면 빈 리스트를 반환한다")
	void parseAreas_emptyResult() {
		List<AreaDTO> areas = tourApiParser.parseAreas("""
			{"response":{"body":{"items":"","totalCount":0}}}
			""");

		assertThat(areas).isEmpty();
	}
}
