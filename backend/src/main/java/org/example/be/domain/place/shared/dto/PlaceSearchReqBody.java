package org.example.be.domain.place.shared.dto;

import java.util.List;

import org.example.be.domain.place.shared.type.PlaceSortType;

public record PlaceSearchReqBody(
	List<String> regions,   // ex) ["강원", "강릉"]
	List<String> themes,    // ex) ["체험 및 액티비티", "미식 여행 및 먹방 중심"]
	String keyword,         // 검색어

	int page, // 기본값 1
	int size, // 기본값 30
	PlaceSortType sortType // 기본값 - 이름 오름차순
) {
	public PlaceSearchReqBody {
		if (page < 1)
			page = 1;
		if (size < 1)
			size = 30;
		if (sortType == null)
			sortType = PlaceSortType.TITLE_ASC;
	}
}
