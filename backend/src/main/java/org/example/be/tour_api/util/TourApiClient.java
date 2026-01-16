package org.example.be.tour_api.util;

import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class TourApiClient {

	private final RestTemplate restTemplate = new RestTemplate();

	// 지역별 관광정보 요청
	public String fetchTourData(int areaCode, int contentTypeId, int numOfRows, int pageNo, String serviceKey) throws
		Exception {
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

		System.out.println("[Debug] Decoded serviceKey: " + serviceKey);
		System.out.println("[Debug] Final URL: " + url);
		URI uri = new URI(url);

		try {
			return restTemplate.getForObject(uri, String.class);
		} catch (RestClientResponseException ex) {
			System.out.println("[TourAPI Error] status=" + ex.getRawStatusCode()
				+ " body=" + ex.getResponseBodyAsString());
			throw ex;
		}
	}

	// 지역 코드 조회 - 모든 areaCode 목록 조회
	public String fetchAreaCodes(String serviceKey) throws Exception {

		String url = UriComponentsBuilder
			.fromHttpUrl("https://apis.data.go.kr/B551011/KorService2/areaCode2")
			.queryParam("MobileOS", "ETC")
			.queryParam("MobileApp", "Test")
			.queryParam("_type", "json")
			.queryParam("serviceKey", serviceKey)
			.build(true)
			.toUriString();

		URI uri = new URI(url);

		try {
			return restTemplate.getForObject(uri, String.class);
		} catch (RestClientResponseException e) {
			System.out.println("[TourAPI Error] status=" + e.getRawStatusCode()
				+ " body=" + e.getResponseBodyAsString());
			throw e;
		}
	}

	// 지역 코드 조회 - 특정 areaCode의 sigungu 목록 조회
	public String fetchSigunguCodes(String areaCode, String serviceKey) throws Exception {

		String url = UriComponentsBuilder
			.fromHttpUrl("https://apis.data.go.kr/B551011/KorService2/areaCode2")
			.queryParam("MobileOS", "ETC")
			.queryParam("MobileApp", "Test")
			.queryParam("_type", "json")
			.queryParam("serviceKey", serviceKey)
			.queryParam("areaCode", areaCode)
			.build(true)
			.toUriString();

		URI uri = new URI(url);

		try {
			return restTemplate.getForObject(uri, String.class);
		} catch (RestClientResponseException e) {
			System.out.println("[TourAPI Error] status=" + e.getRawStatusCode()
				+ " body=" + e.getResponseBodyAsString());
			throw e;
		}

	}

}
