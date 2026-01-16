// package org.example.be.tour_api.controller;
//
// import org.example.be.response.CommonResponse;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RestController;
//
// import lombok.RequiredArgsConstructor;
//
// // 지역 정보 매핑 테이블 최신화 관리 컨트롤러
// @RestController
// @RequiredArgsConstructor
// public class RefreshRegionController {
//
// 	private final RefreshRegionService refreshRegionService;
//
// 	// 지역 정보 매핑 테이블 최신화
// 	/*
// 	param : void
// 	return : void
//
// 	flow :
// 	1. TourAPI에 지역 코드 정보 요청
// 	2. 반환받은 JSON 파싱하여 Toleave 지역 분류
// 	3. 새로운 정보 및 변화된 정보 저장
//
// 	 */
// 	@GetMapping("/getRefreshedRegion")
// 	public ResponseEntity<CommonResponse<Void>> RefreshRegion() {
// 		refreshRegionService.getRefreshedRegion();
// 		return ResponseEntity.ok(CommonResponse.success(null, "지역 매핑 테이블 갱신 성공"));
// 	}
//
// }
