package org.example.be.tour_api.controller;

import org.example.be.response.CommonResponse;
import org.example.be.tour_api.dto.FetchResult;
import org.example.be.tour_api.service.RefreshCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 카테고리 업데이트 (theme) 수동 트리거 매핑 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/tourism/refresh")
public class RefreshCategoryController {

	private final RefreshCategoryService refreshCategoryService;

	/**
	 * 카테고리 매핑 테이블 최신화
	 *
	 * flow:
	 * 1. TourAPI categoryCode2 contentTypeId별로 3단계(ca1 -> 2 -> 3) 순회
	 * 2. 카테고리 코드 및 분류명을 파싱하여 theme 자동 분류
	 * 3. PlaceCategory 테이블에 신규 저장 또는 변경사항 업데이트
	 */
	@GetMapping("/category")
	public ResponseEntity<CommonResponse<FetchResult>> refreshCategory() {
		FetchResult result = refreshCategoryService.refreshCategories();
		return ResponseEntity.ok(CommonResponse.success(result, "카테고리 매핑 테이블 갱신 성공"));
	}
}
