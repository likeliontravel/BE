package org.example.be.external.tourapi.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.example.be.external.tourapi.dto.AreaDTO;
import org.example.be.external.tourapi.dto.CategoryCodeDTO;
import org.example.be.external.tourapi.dto.SigunguDTO;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.CommonErrorCode;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TourApiParser {

	// 파싱 실패 시 예외 메시지에 실을 응답 본문의 최대 길이. 응답 형식이 바뀌었는지 판단하기에는 앞부분이면 충분하다.
	private static final int MAX_LOGGED_JSON_LENGTH = 200;

	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * TourAPI 응답의 response.body.items.item 을 꺼낸다. 결과가 1개면 객체, 2개 이상이면 배열로 오므로 둘 다 리스트로 맞춘다.
	 *
	 * '정상 0건' 과 '파싱 실패' 를 구분한다.
	 * - 정상 0건 : items 가 없거나 빈 문자열("") 이다. TourAPI 는 결과가 없으면 items 를 객체가 아니라 "" 로 보낸다. 빈 리스트를 반환한다.
	 * - 파싱 실패 : JSON 이 아니거나(게이트웨이 XML/HTML 오류 등) 기대한 구조가 아니다. 예외를 던진다.
	 * 빈 리스트로 삼키면 Reader 가 '0건 수집'으로 인식해 수집 실패 집계(TourDataJobListener 판정)에 잡히지 않고,
	 * TourAPI 응답 형식 변경이 Job COMPLETED 뒤로 은폐된다.
	 *
	 * 여기서는 로그를 남기지 않는다. 받는 쪽이 이미 ERROR + 스택으로 기록한다.
	 * 배치 Reader 는 catch (BusinessException) 에서, Refresh Step 은 AbstractStep 이, HTTP 는 BusinessExceptionHandler 가 남긴다.
	 *
	 * @throws BusinessException 응답을 해석할 수 없을 때 EXTERNAL_API_FAILED(502)
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> parseItems(String json) {
		if (json == null) {
			throw parseFailure("응답 본문 없음", null, null);
		}

		Map<String, Object> root;
		try {
			root = objectMapper.readValue(json, Map.class);
		} catch (JsonProcessingException e) {
			throw parseFailure("JSON 아님", json, e);
		}

		if (root == null
			|| !(root.get("response") instanceof Map<?, ?> response)
			|| !(response.get("body") instanceof Map<?, ?> body)) {
			throw parseFailure("response.body 구조 없음", json, null);
		}

		Object itemsObj = body.get("items");
		if (itemsObj == null || (itemsObj instanceof String itemsText && itemsText.isBlank())) {
			// 정상 0건
			return Collections.emptyList();
		}

		if (!(itemsObj instanceof Map<?, ?> items)) {
			throw parseFailure("items 형식 아님", json, null);
		}

		Object itemObj = items.get("item");
		if (itemObj == null) {
			return Collections.emptyList();
		}
		if (itemObj instanceof List) {
			return (List<Map<String, Object>>)itemObj;
		}
		if (itemObj instanceof Map) {
			return List.of((Map<String, Object>)itemObj);
		}
		throw parseFailure("item 형식 아님", json, null);
	}

	// 파싱 실패를 외부 연동 실패(502)로 변환한다. 응답 앞부분은 디버깅용 메시지에만 싣는다(클라이언트에는 ErrorCode 메시지만 노출).
	private BusinessException parseFailure(String reason, String json, Throwable cause) {
		return new BusinessException(CommonErrorCode.EXTERNAL_API_FAILED,
			"TourAPI 응답 파싱 실패(" + reason + "). json 앞부분=" + LogFormatUtils.formatValue(json, MAX_LOGGED_JSON_LENGTH,
				true),
			cause);
	}

	// AreaCode, AreaName 받아 파싱
	public List<AreaDTO> parseAreas(String json) {
		List<Map<String, Object>> items = parseItems(json);

		List<AreaDTO> result = new ArrayList<>();
		for (Map<String, Object> item : items) {
			String code = String.valueOf(item.getOrDefault("code", ""));
			String name = String.valueOf(item.getOrDefault("name", ""));

			if (!code.isBlank()) {
				result.add(AreaDTO.builder()
					.areaCode(code)
					.areaName(name)
					.build());
			}
		}
		return result;
	}

	// 특정 AreaCode에 대한 siGunGuCode, siGunGuName 받아 파싱
	public List<SigunguDTO> parseSigungus(String json, String areaCode) {
		List<Map<String, Object>> items = parseItems(json);

		List<SigunguDTO> result = new ArrayList<>();
		for (Map<String, Object> item : items) {
			String code = String.valueOf(item.getOrDefault("code", ""));
			String name = String.valueOf(item.getOrDefault("name", ""));
			if (!code.isBlank()) {
				result.add(SigunguDTO.builder()
					.areaCode(areaCode)
					.siGunGuCode(code)
					.siGunGuName(name)
					.build());
			}
		}
		return result;
	}

	// categoryCode2 API 응답 파싱 -> CategoryCodeDTO 리스트
	public List<CategoryCodeDTO> parseCategories(String json) {

		List<Map<String, Object>> items = parseItems(json);

		List<CategoryCodeDTO> result = new ArrayList<>();

		for (Map<String, Object> item : items) {
			String code = String.valueOf(item.getOrDefault("code", ""));
			String name = String.valueOf(item.getOrDefault("name", ""));

			if (!code.isBlank()) {
				result.add(new CategoryCodeDTO(code, name));
			}
		}
		return result;
	}
}
