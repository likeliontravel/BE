package org.example.be.place.region;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TourRegionService {

    private final TourRegionRepository tourRegionRepository;

    // ** 장소 각 구현체 생성(저장) 시 지역키워드 가져와서 컬럼으로 같이 저장하는거 꼭 추가해야함 ** //
    // 지역 코드, 시군구 코드로 해당 지역키워드(지원하는 지역) 가져오기
    public String getRegionKeywordByAreaCodeAndSiGunGuCode(String areaCode, String siGunGuCode) {

        Optional<TourRegion> tourRegionOptional = tourRegionRepository.findByAreaCodeAndSiGunGuCode(areaCode, siGunGuCode);
        return tourRegionOptional.map(TourRegion::getRegionKeyword)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역코드와 시군구코드로 지역을 찾을 수 없습니다.\n" +
                        "areaCode: " + areaCode + "\nsiGunGuCode: " + siGunGuCode));

    }

}
