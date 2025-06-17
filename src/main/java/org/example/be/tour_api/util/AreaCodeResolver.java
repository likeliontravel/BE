package org.example.be.tour_api.util;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AreaCodeResolver {

    private static final Map<Integer, String> areaCodeMap = Map.ofEntries(
            Map.entry(1, "서울"),
            Map.entry(2, "인천"),
            Map.entry(3, "대전"),
            Map.entry(4, "대구"),
            Map.entry(5, "광주"),
            Map.entry(6, "부산"),
            Map.entry(7, "울산"),
            Map.entry(8, "세종"),
            Map.entry(31, "경기도"),
            Map.entry(32, "강원"),
            Map.entry(33, "충청북도"),
            Map.entry(34, "충청남도"),
            Map.entry(35, "경상북도"),
            Map.entry(36, "경상남도"),
            Map.entry(37, "전북"),
            Map.entry(38, "전라남도"),
            Map.entry(39, "제주")
    );

    public String getState(int code) {
        return areaCodeMap.get(code);
    }
}
