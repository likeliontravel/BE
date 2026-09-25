package org.example.be.external.tourapi.batch.listener;

import java.util.Map;

import org.example.be.domain.place.shared.entity.Place;
import org.springframework.batch.core.SkipListener;

import lombok.extern.slf4j.Slf4j;

/**
 * chunk Step(관광지, 식당, 숙박) 에서 skip 된 아이템을 기록하는 SkipListener
 *
 * Spring Batch 는 skip 한 아이템을 debug 로만 남긴다. 운영 로그 레벨(INFO)에서는 흔적이 없어
 * "몇 건이, 왜 버려졌는지" 를 알 수 없었다. 이 리스너가 skip 마다 WARN 한 줄 + 스택을 남긴다.
 * skip 은 Step 당 tourapi.batch.skip-limit 건까지만 허용되므로 로그 양도 그만큼으로 제한된다.
 *
 * 입력 타입은 Reader 가 넘기는 TourAPI 응답 1건 (Map), 출력 타입은 세 엔티티의 공통 부모(Place) 라
 * 인스턴스 하나를 세 Step 에 함께 등록한다.
 */
@Slf4j
public class PlaceSkipListener implements SkipListener<Map<String, Object>, Place> {

	// TourApiItemReader 는 open() 에서 미리 수집하고 read() 는 List 에서 꺼내기만 하므로 평시에는 발생하지 않는다.
	@Override
	public void onSkipInRead(Throwable t) {
		log.warn("[TourDataSkip] read 단계 skip", t);
	}

	@Override
	public void onSkipInProcess(Map<String, Object> item, Throwable t) {
		log.warn("[TourDataSkip] process 단계 skip - contentId={}", item.get("contentid"), t);
	}

	@Override
	public void onSkipInWrite(Place item, Throwable t) {
		log.warn("[TourDataSkip] write 단계 skip - contentId={}", item.getContentId(), t);
	}
}
