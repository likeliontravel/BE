package org.example.be.tour_api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.example.be.tour_api.dto.AreaDTO;
import org.example.be.tour_api.dto.CategoryCodeDTO;
import org.example.be.tour_api.dto.SigunguDTO;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TourApiParser {

	private final ObjectMapper objectMapper = new ObjectMapper();

	// TourAPI로부터 받은 json 파싱 : ObjectMapper를 이용해 Map 타입으로 파싱. 결과가 1개인 경우와 2개이상의 List로 구성되는 경우 전부 처리 가능
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> parseItems(String json) {
		try {
			Map<String, Object> map = objectMapper.readValue(json, Map.class);
			Map<String, Object> response = (Map<String, Object>)map.get("response");
			Map<String, Object> body = (Map<String, Object>)response.get("body");
			Map<String, Object> items = (Map<String, Object>)body.get("items");

			if (items == null)
				return Collections.emptyList();

			Object itemObj = items.get("item");
			if (itemObj == null)
				return Collections.emptyList();

			if (itemObj instanceof List) {
				return (List<Map<String, Object>>)itemObj;
			}

			if (itemObj instanceof Map) {
				return List.of((Map<String, Object>)itemObj);
			}

			return Collections.emptyList();
		} catch (Exception e) {
			return Collections.emptyList();
		}
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
