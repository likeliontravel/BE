package org.example.be.tour_api.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.place.region.TourRegion;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.tour_api.dto.AreaDTO;
import org.example.be.tour_api.dto.SigunguDTO;
import org.example.be.tour_api.util.RegionClassifier;
import org.example.be.tour_api.util.TourApiClient;
import org.example.be.tour_api.util.TourApiParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 지역 코드 최신화 로직 서비스레이어
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshRegionService {

	private final TourApiClient tourApiClient;
	private final TourApiParser tourApiParser;
	private final TourRegionRepository tourRegionRepository;
	private final RegionClassifier regionClassifier;

	@Value("${service-key}")
	private String serviceKey;

	// 지역 코드 정보 갱신
	@Transactional
	public void refreshRegions() {

		try {
			// 1. 모든 areaCode 조회
			String areaJson = tourApiClient.fetchAreaCodes(serviceKey);
			List<AreaDTO> areas = tourApiParser.parseAreas(areaJson);

			log.info("[RefreshRegion] 조회된 area 수 : {}", areas.size());

			int totalSaved = 0;        // 총 저장된 수
			int totalUpdated = 0;    // 총 변경된 수
			int totalSkipped = 0;    // 기존 정보가 다르지 않아 보존된 수

			// 2. 각 areaCode에 대해 sigungu 조회 및 저장
			for (AreaDTO area : areas) {
				String areaCode = area.getAreaCode();
				String areaName = area.getAreaName();

				String sigunguJson = tourApiClient.fetchSigunguCodes(areaCode, serviceKey);
				List<SigunguDTO> sigungus = tourApiParser.parseSigungus(sigunguJson, areaCode);

				for (SigunguDTO sigungu : sigungus) {
					String siGunGuCode = sigungu.getSiGunGuCode();
					String siGunGuName = sigungu.getSiGunGuName();

					// 지역 분류
					String region = regionClassifier.classify(areaName, siGunGuName);

					// DB upsert 수행
					int[] result = upsertTourRegion(
						areaCode, areaName, siGunGuCode, siGunGuName, region
					);
					totalSaved += result[0];
					totalUpdated += result[1];
					totalSkipped += result[2];
				}
			}

			// 3. 모든 areaCode에 대해 siGunGuCode = 99 (기타) 행 일괄 추가
			for (AreaDTO area : areas) {
				int[] result = upsertTourRegion(
					area.getAreaCode(),
					area.getAreaName(),
					"99",
					"기타",
					"기타"
				);
				totalSaved += result[0];
				totalUpdated += result[1];
				totalSkipped += result[2];
			}

			log.info(
				"[RefreshRegion] 완료 - 신규 : {}, 변경 : {}, 무변경 : {}",
				totalSaved, totalUpdated, totalSkipped
			);

		} catch (Exception e) {
			log.error("[RefreshRegion] 지역코드 갱신 실패", e);
			throw new ResourceUpdateException("지역 정보 갱신 중 오류 발생", e);
		}
	}

	/**
	 * TourRegion upsert ( 변경 시에만 업데이트 )
	 * 변경된 정보는 업데이트하여 저장, saved, updated, skipped 반환
	 */
	private int[] upsertTourRegion(
		String areaCode,
		String areaName,
		String siGunGuCode,
		String siGunGuName,
		String region
	) {
		Optional<TourRegion> existing = tourRegionRepository.findByAreaCodeAndSiGunGuCode(areaCode, siGunGuCode);

		if (existing.isPresent()) {
			TourRegion t = existing.get();

			// 변경 여부 확인
			boolean changed =
				!Objects.equals(areaName, t.getAreaName())
					|| !Objects.equals(siGunGuName, t.getSiGunGuName())
					|| !Objects.equals(region, t.getRegion());

			if (changed) {
				t.setAreaName(areaName);
				t.setSiGunGuName(siGunGuName);
				t.setRegion(region);
				// dirty checking으로 자동 업데이트
				return new int[] {0, 1, 0};
			} else {
				return new int[] {0, 0, 1};
			}
		} else {
			TourRegion tourRegion = TourRegion.builder()
				.areaCode(areaCode)
				.areaName(areaName)
				.siGunGuCode(siGunGuCode)
				.siGunGuName(siGunGuName)
				.region(region)
				.build();
			tourRegionRepository.save(tourRegion);
			return new int[] {1, 0, 0};
		}
	}
}
