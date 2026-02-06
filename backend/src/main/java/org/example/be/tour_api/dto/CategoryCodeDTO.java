package org.example.be.tour_api.dto;

/**
 * /categoryCode2 API 응답 항목 DTO
 * cat1, cat2, cat3 모든 레벨에서 공용으로 사용
 *
 * @param code 카테고리 코드 (cat1: "A01", cat2: "A0101", cat3: "A01010100")
 * @param name 카테고리 이름 (cat1: "자연", cat2: "자연관광지", cat3: "국립공원")
 */
public record CategoryCodeDTO(String code, String name) {
}
