package org.example.be.tour_api.batch.writer;

import java.util.List;

import org.example.be.place.restaurant.entity.Restaurant;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Restaurant 배치 저장을 위한 ItemWriter
 *
 * 역할:
 * - Processor에서 전달받은 Restaurant 엔티티들을 일괄 저장
 * - 신규 엔티티: INSERT
 * - 기존 엔티티(Processor에서 필드 업데이트됨): JPA dirty checking으로 UPDATE
 *
 * Chunk단위로 동작; chunk 크기 100:
 * - 100개씩 모아서 한 트랜잭션에 커밋
 */
@Slf4j
@RequiredArgsConstructor
public class RestaurantItemWriter implements ItemWriter<Restaurant> {

	private final RestaurantRepository restaurantRepository;

	@Override
	public void write(Chunk<? extends Restaurant> chunk) throws Exception {
		List<? extends Restaurant> items = chunk.getItems();

		if (!items.isEmpty()) {
			restaurantRepository.saveAll(items);
			log.debug("[Writer] Restaurant {}건 저장 완료", items.size());
		}
	}
}
