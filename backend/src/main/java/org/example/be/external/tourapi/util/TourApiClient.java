package org.example.be.external.tourapi.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class TourApiClient {

	private final RestTemplate restTemplate = new RestTemplate();
	private final TourApiParser tourApiParser;

	public TourApiClient(TourApiParser tourApiParser) {
		this.tourApiParser = tourApiParser;
	}

	/**
	 * 하나의 areaCode에 대해 모든 페이지를 순회하며 데이터 수집
	 * @param areaCode 지역코드
	 * @param contentTypeId 관광 타입
	 * @param numOfRows 페이지당 조회 건수
	 * @param serviceKey 서비스 인증 키
	 * @return 해당 areaCode의 전체 파싱된 데이터 목록
	 * @throws BusinessException TourAPI 호출 실패 시 EXTERNAL_API_FAILED(502)
	 */
	public List<Map<String, Object>> fetchAllPagesForArea(
		int areaCode, int contentTypeId, int numOfRows, String serviceKey
	) {

		List<Map<String, Object>> allItems = new ArrayList<>();
		int pageNo = 1;

		while (true) {
			String json = fetchTourData(areaCode, contentTypeId, numOfRows, pageNo, serviceKey);
			List<Map<String, Object>> items = tourApiParser.parseItems(json);

			if (items.isEmpty())
				break;
			allItems.addAll(items);
			if (items.size() < numOfRows)
				break;
			pageNo++;
		}

		return allItems;
	}

	// 지역별 관광정보 요청
	public String fetchTourData(int areaCode, int contentTypeId, int numOfRows, int pageNo, String serviceKey) {
		//        String decodedServiceKey = URLDecoder.decode(serviceKey, StandardCharsets.UTF_8);

		String url = UriComponentsBuilder.fromHttpUrl("https://apis.data.go.kr/B551011/KorService2/areaBasedList2")
			.queryParam("MobileOS", "ETC")
			.queryParam("MobileApp", "Test")
			.queryParam("_type", "json")
			.queryParam("areaCode", areaCode)
			.queryParam("contentTypeId", contentTypeId)
			.queryParam("numOfRows", numOfRows)
			.queryParam("pageNo", pageNo)
			.queryParam("serviceKey", serviceKey)
			.build(true)
			.toUriString();

		// TourAPI 요청 URL 로깅
		// System.out.println("[Debug] Decoded serviceKey: " + serviceKey);
		// System.out.println("[Debug] Final URL: " + url);

		// URI.create 는 new URI(url) 과 같지만 URISyntaxException(checked)을 IllegalArgumentException 으로 감싼다.
		// url 은 우리 코드가 조립하므로 여기서의 실패는 외부 장애가 아니라 코드 결함(500)이다 -> throws 선언 삭제했다. (2026.09.17)
		URI uri = URI.create(url);

		try {
			return restTemplate.getForObject(uri, String.class);
		} catch (RestClientResponseException ex) {
			System.out.println("[TourAPI Error] status=" + ex.getRawStatusCode()
				+ " body=" + ex.getResponseBodyAsString());
			throw new BusinessException(ErrorCode.EXTERNAL_API_FAILED,
				"TourAPI 관광정보 조회 실패. areaCode: " + areaCode + ", contentTypeId: " + contentTypeId
					+ ", pageNo: " + pageNo, ex);
		} catch (RestClientException ex) {
			// 응답조차 받지 못한 실패(연결 거부 또는 타임아웃, DNS - ResourceAccessException)도 외부 연동 실패라 502로 통일한다.
			// 여기서는 로그를 남기지 않는다. HTTP 경로에서는 BusinessExceptionHandler 가 5xx 를 ERROR + 스택으로 기록하고 있다.
			throw new BusinessException(ErrorCode.EXTERNAL_API_FAILED,
				"TourAPI 관광정보 조회 실패. areaCode: " + areaCode + ", contentTypeId: "
					+ contentTypeId + ", pageNo: " + pageNo, ex);
		}
	}

	// 지역 코드 조회 - 모든 areaCode 목록 조회
	public String fetchAreaCodes(String serviceKey) {

		String url = UriComponentsBuilder
			.fromHttpUrl("https://apis.data.go.kr/B551011/KorService2/areaCode2")
			.queryParam("MobileOS", "ETC")
			.queryParam("MobileApp", "Test")
			.queryParam("_type", "json")
			.queryParam("numOfRows", 9999)
			.queryParam("serviceKey", serviceKey)
			.build(true)
			.toUriString();

		URI uri = URI.create(url);

		try {
			return restTemplate.getForObject(uri, String.class);
		} catch (RestClientResponseException e) {
			System.out.println("[TourAPI Error] status=" + e.getRawStatusCode()
				+ " body=" + e.getResponseBodyAsString());
			throw new BusinessException(ErrorCode.EXTERNAL_API_FAILED, "TourAPI 지역코드 조회 실패", e);
		} catch (RestClientException e) {
			// 연결 거부 또는 타임아웃, DNS 실패 (상세: fetchTourData 의 같은 catch)
			throw new BusinessException(ErrorCode.EXTERNAL_API_FAILED, "TourAPI 지역코드 조회 실패", e);
		}
	}

	// 지역 코드 조회 - 특정 areaCode의 sigungu 목록 조회
	public String fetchSigunguCodes(String areaCode, String serviceKey) {

		String url = UriComponentsBuilder
			.fromHttpUrl("https://apis.data.go.kr/B551011/KorService2/areaCode2")
			.queryParam("MobileOS", "ETC")
			.queryParam("MobileApp", "Test")
			.queryParam("_type", "json")
			.queryParam("serviceKey", serviceKey)
			.queryParam("areaCode", areaCode)
			.queryParam("numOfRows", 9999)
			.build(true)
			.toUriString();

		URI uri = URI.create(url);

		try {
			return restTemplate.getForObject(uri, String.class);
		} catch (RestClientResponseException e) {
			System.out.println("[TourAPI Error] status=" + e.getRawStatusCode()
				+ " body=" + e.getResponseBodyAsString());
			throw new BusinessException(ErrorCode.EXTERNAL_API_FAILED,
				"TourAPI 시군구코드 조회 실패. areaCode: " + areaCode, e);
		} catch (RestClientException e) {
			// 연결 거부 또는 타임아웃, DNS 실패 (상세: fetchTourData 의 같은 catch)
			throw new BusinessException(ErrorCode.EXTERNAL_API_FAILED,
				"TourAPI 시군구코드 조회 실패. areaCode: " + areaCode, e);
		}

	}

	// 카테고리 코드 조회 - categoryCode2 API
	// cat1, cat2가 null이면 상위 레벨 목록 조회
	public String fetchCategoryCodes(String contentTypeId, String cat1, String cat2, String serviceKey) {
		UriComponentsBuilder builder = UriComponentsBuilder
			.fromHttpUrl("https://apis.data.go.kr/B551011/KorService2/categoryCode2")
			.queryParam("MobileOS", "ETC")
			.queryParam("MobileApp", "Test")
			.queryParam("_type", "json")
			.queryParam("contentTypeId", contentTypeId)
			.queryParam("numOfRows", 9999)
			.queryParam("serviceKey", serviceKey);

		if (cat1 != null) {
			builder.queryParam("cat1", cat1);
		}
		if (cat2 != null) {
			builder.queryParam("cat2", cat2);
		}

		String url = builder.build(true)
			.toUriString();

		URI uri = URI.create(url);

		try {
			return restTemplate.getForObject(uri, String.class);
		} catch (RestClientResponseException e) {
			System.out.println("[TourAPI Error] status=" + e.getRawStatusCode()
				+ " body=" + e.getResponseBodyAsString());
			throw new BusinessException(ErrorCode.EXTERNAL_API_FAILED,
				"TourAPI 분류코드 조회 실패. contentTypeId: " + contentTypeId + ", cat1: " + cat1 + ", cat2: " + cat2, e);
		} catch (RestClientException e) {
			// 연결 거부 또는 타임아웃, DNS 실패 (상세: fetchTourData 의 같은 catch)
			throw new BusinessException(ErrorCode.EXTERNAL_API_FAILED,
				"TourAPI 분류코드 조회 실패. contentTypeId: " + contentTypeId + ", cat1: " + cat1 + ", cat2: " + cat2, e);
		}

	}

}
