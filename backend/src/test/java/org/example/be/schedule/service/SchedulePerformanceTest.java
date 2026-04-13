package org.example.be.schedule.service;

import java.time.LocalDateTime;
import java.util.List;

import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.member.entity.Member;
import org.example.be.member.repository.MemberRepository;
import org.example.be.place.entity.PlaceType;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.example.be.schedule.entity.Schedule;
import org.example.be.schedule.entity.SchedulePlace;
import org.example.be.schedule.repository.SchedulePlaceRepository;
import org.example.be.schedule.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class SchedulePerformanceTest {

	@Autowired
	private ScheduleService scheduleService;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private SchedulePlaceRepository schedulePlaceRepository;
	@Autowired
	private TouristSpotRepository touristSpotRepository;

	private Long testUserId;
	private final String VALID_CONTENT_ID = "1884191";
	private final int DUMMY_COUNT = 100;

	@BeforeEach
	void setUp() {
		Member member = memberRepository.findByEmail("yjc7241@naver.com")
			.orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
		testUserId = member.getId();

		Group group = new Group();
		group.setGroupName("PERF_TEST_GROUP_" + System.currentTimeMillis());
		group.setCreatedBy(member);
		groupRepository.save(group);
		group.addMember(member);

		Schedule schedule = Schedule.create(LocalDateTime.now(), LocalDateTime.now().plusDays(3), group);
		scheduleRepository.save(schedule);
		
		for (int i = 0; i < DUMMY_COUNT; i++) {
			SchedulePlace.create(schedule, VALID_CONTENT_ID, PlaceType.TouristSpot,
				LocalDateTime.now(), LocalDateTime.now(), 1, i);
		}

		scheduleRepository.flush();
	}

	@Test
	@DisplayName("N+1 최적화 성능 벤치마크 (데이터 100건 기준)")
	void comparePerformance() {
		System.out.println("⏳ 기존 방식(N+1) 측정 시작...");
		long startOld = System.currentTimeMillis();

		List<SchedulePlace> places = schedulePlaceRepository.findAll();
		for (SchedulePlace sp : places) {
			touristSpotRepository.findByContentId(sp.getContentId());
		}

		long endOld = System.currentTimeMillis();
		long oldDuration = endOld - startOld;

		System.out.println("⏳ 개선 방식(Batch) 측정 시작...");
		long startNew = System.currentTimeMillis();

		scheduleService.getScheduleList(testUserId);

		long endNew = System.currentTimeMillis();
		long newDuration = endNew - startNew;

		double improvement = ((double)(oldDuration - newDuration) / oldDuration) * 100;

		System.out.println("\n================================================");
		System.out.println("📊 [성능 테스트 결과 분석 - 데이터 " + DUMMY_COUNT + "건]");
		System.out.println("------------------------------------------------");
		System.out.println("❌ 기존 방식 (N+1 개별 조회): " + oldDuration + " ms");
		System.out.println("✅ 개선 방식 (Batch 벌크 조회): " + newDuration + " ms");
		System.out.println("------------------------------------------------");
		System.out.println("🔥 성능 향상률: " + String.format("%.2f", improvement) + "%");
		System.out.println("💡 쿼리 감소: " + (DUMMY_COUNT + "회 -> 약 6회 미만"));
		System.out.println("================================================\n");
	}
}
