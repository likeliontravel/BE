package org.example.be.tour_api.batch.writer;

import java.util.List;

import org.example.be.place.accommodation.entity.Accommodation;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Accommodation 배치 저장을 위한 ItemWriter
 *
 * 역할:
 * - Processor에서 전달받은 Accommodation 엔티티들을 일괄 저장
 * - 신규 엔티티: INSERT
 * - 기존 엔티티(Processor에서 필드 업데이트됨): JPA dirty checking으로 UPDATE
 *
 * Chunk단위로 동작; Chunk 크기 100:
 * - 100개씩 모아서 한 트랜잭션에 커밋
 */
@Slf4j
@RequiredArgsConstructor
public class AccommodationItemWriter implements ItemWriter<Accommodation> {

	private final AccommodationRepository accommodationRepository;

	@Override
	public void write(Chunk<? extends Accommodation> chunk) throws Exception {
		List<? extends Accommodation> items = chunk.getItems();

		if (!items.isEmpty()) {
			accommodationRepository.saveAll(items);
			log.debug("Accommodation {}건 저장 완료", items.size());
		}
	}
}
