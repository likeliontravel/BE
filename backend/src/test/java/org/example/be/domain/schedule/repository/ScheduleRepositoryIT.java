package org.example.be.domain.schedule.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.example.be.domain.group.entity.Group;
import org.example.be.domain.group.repository.GroupRepository;
import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.repository.MemberRepository;
import org.example.be.domain.schedule.entity.Schedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;

@SpringBootTest
@Tag("integration")
@Transactional
@DisplayName("ScheduleRepository 가장 가까운 예정 일정 QueryDSL 통합 테스트")
class ScheduleRepositoryIT {

	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private EntityManager entityManager;

	@Test
	@DisplayName("과거 일정을 제외하고 가장 가까운 일정과 그룹을 한 번에 조회한다")
	void findNearestUpcoming_excludesPastAndFetchJoinsGroup() {
		String suffix = UUID.randomUUID().toString();
		Member member = memberRepository.save(
			Member.createForJoin("nearest-" + suffix + "@example.com", "tester", "password")
		);

		saveSchedule(member, "past-" + suffix, LocalDateTime.of(2026, 7, 15, 9, 0));
		saveSchedule(member, "nearest-" + suffix, LocalDateTime.of(2026, 7, 18, 18, 0));
		saveSchedule(member, "far-" + suffix, LocalDateTime.of(2026, 7, 25, 9, 0));

		entityManager.flush();
		entityManager.clear();

		Schedule result = scheduleRepository.findNearestUpcomingByMemberId(
			member.getId(),
			LocalDateTime.of(2026, 7, 16, 0, 0)
		).orElseThrow();

		assertThat(result.getStartSchedule()).isEqualTo(LocalDateTime.of(2026, 7, 18, 18, 0));
		assertThat(result.getGroup().getGroupName()).isEqualTo("nearest-" + suffix);

		PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
		assertThat(persistenceUnitUtil.isLoaded(result.getGroup())).isTrue();
	}

	private void saveSchedule(Member member, String groupName, LocalDateTime startSchedule) {
		Group group = groupRepository.save(Group.create(groupName, member, "test group"));
		scheduleRepository.save(Schedule.create(startSchedule, startSchedule.plusDays(1), group));
	}
}
