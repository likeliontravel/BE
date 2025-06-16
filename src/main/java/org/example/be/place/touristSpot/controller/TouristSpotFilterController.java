package org.example.be.place.touristSpot.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.be.place.entity.PlaceSortType;
import org.example.be.place.touristSpot.dto.TouristSpotDTO;
import org.example.be.place.touristSpot.service.TouristSpotFilterService;
import org.example.be.response.CommonResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class TouristSpotFilterController {

    private final TouristSpotFilterService touristSpotFilterService;


    /*

    예찬 선배가 잘해놔서 뭐 다른거 없었고 그냥 어노테이션 관련해서 추가해봤고 페이징 처리 할때


    페이지 번호는 보통 1부터 시작한다고 가정하는데
    사용자가 0이나 음수를 입력할 경우는 비정상적인 요청이기 때문에 @Min(1)으로 막아주는 기능을 추가했습니다.

    그리고 사이즈 부분은
    0 이하의 값을 넣으면 데이터가 없거나, 쿼리가 비정상적으로 동작할 수 있기 때문에 최소 1 이상으로 제한했습ㄴ니다.

    한마디로

    클라이언트가 해당 값을 전달하지 않아도 기본적으로 page=1, size=30으로 동작하게 하여,
    페이징 없는 단순 조회처럼도 동작할 수 있도록 했습니다.

    @Min(1) 어노테이션을 사용하여 page와 size 파라미터가 최소 1 이상의 값만 입력되도록 유효성 검사를 적용했습니다.

    이를 통해 클라이언트가 0 이하의 값을 입력하는 경우, globalExceptionHandler 에서 처리되어 사용자에게 명확한 오류 메시지를 반환할 수 있습니다.

    이러한 유효성 검사는 잘못된 요청으로 인해 발생할 수 있는 의도치 않은 동작 예시로 빈 응답, Index 오류 사전에 방지할 수 있어 서비스 안정성 측면에서 유리하다고 생각합니다.

     이건 찾아봤습니다.

    */

    @GetMapping("/touristspots")
    public ResponseEntity<CommonResponse<List<TouristSpotDTO>>> getFilteredTouristSpots(
            @RequestParam(required = false) List<String> regions,
            @RequestParam(required = false) List<String> themes,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,   // 1 이상만 허용
            @RequestParam(defaultValue = "30") @Min(1) int size,  // 1 이상만 허용
            @RequestParam(defaultValue = "TITLE_ASC") PlaceSortType sortType
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortType.getSortDirection(), sortType.getSortProperty()));
        List<TouristSpotDTO> results = touristSpotFilterService.getFilteredTouristSpots(regions, themes, keyword, pageable);
        return ResponseEntity.ok(CommonResponse.success(results, "관광지 필터링 조회 성공"));
    }
}