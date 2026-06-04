package org.example.be.domain.schedule.dto.response;

public record PlaceSimpleResBody(
	String title,
	String img,
	String address
) {
	public static PlaceSimpleResBody from(String title, String img, String addr1, String addr2) {
		String fullAddress =
			(addr1 != null ? addr1 : "") + (addr1 != null && addr2 != null ? " " : "") + (addr2 != null ? addr2 : "");

		return new PlaceSimpleResBody(title, img, fullAddress);
	}
}
