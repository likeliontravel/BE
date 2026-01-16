package org.example.be.tour_api.test;

import java.util.List;

import org.example.be.tour_api.dto.AreaDTO;
import org.example.be.tour_api.dto.SigunguDTO;
import org.example.be.tour_api.util.TourApiClient;
import org.example.be.tour_api.util.TourApiParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourApiRefreshRegionTestRunner implements CommandLineRunner {

	private final TourApiClient tourApiClient;
	private final TourApiParser tourApiParser;

	@Value("${service-key}")
	private String serviceKey;

	@Override
	public void run(String... args) throws Exception {
		String areaJson = tourApiClient.fetchAreaCodes(serviceKey);
		List<AreaDTO> areas = tourApiParser.parseAreas(areaJson);

		System.out.println("[TourAPI] areas size = " + areas.size());
		if (!areas.isEmpty()) {
			System.out.println("[TourAPI] first area = " + areas.get(0).getAreaCode());
		}

		if (!areas.isEmpty()) {
			String firstAreaCode = areas.get(0).getAreaCode();
			String sigunguJson = tourApiClient.fetchSigunguCodes(firstAreaCode, serviceKey);
			List<SigunguDTO> sigungus = tourApiParser.parseSigungus(sigunguJson, firstAreaCode);

			System.out.println("[TourAPI] sigungu size for areaCode=" + firstAreaCode + "=> " + sigungus.size());
			if (!sigungus.isEmpty()) {
				System.out.println(
					"[TourAPI] first sigungu = " + sigungus.get(0).getSiGunGuCode() + " / " + sigungus.get(0)
						.getSiGunGuName());
			}
		}
	}
}
