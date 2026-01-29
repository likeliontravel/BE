package org.example.be.tour_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FetchResult(int saved, int updated, int skipped, int failed) {

	@JsonProperty("total") // 자동으로 record 이 메서드 반환값을 'total'이라는 키로 json에 포함
	public int total() {
		return saved + updated + skipped + failed;
	}

}
