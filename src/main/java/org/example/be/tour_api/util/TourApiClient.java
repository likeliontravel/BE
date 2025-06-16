package org.example.be.tour_api.util;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class TourApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String fetchTourData(int areaCode, int contentTypeId, int numOfRows, int pageNo, String serviceKey) throws Exception {
//        String decodedServiceKey = URLDecoder.decode(serviceKey, StandardCharsets.UTF_8);

        String url = UriComponentsBuilder.fromHttpUrl("https://apis.data.go.kr/B551011/KorService1/areaBasedList1")
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
        return restTemplate.getForObject(uri, String.class);
    }
}
