package org.example.be.domain.group.announcement.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.example.be.domain.group.announcement.dto.GroupAnnouncementDeleteReqBody;
import org.example.be.domain.group.announcement.dto.GroupAnnouncementDeleteResBody;
import org.example.be.domain.group.announcement.entity.GroupAnnouncement;
import org.example.be.domain.group.announcement.repository.GroupAnnouncementRepository;
import org.example.be.domain.group.entity.Group;
import org.example.be.domain.group.repository.GroupRepository;
import org.example.be.domain.group.service.GroupService;
import org.example.be.domain.member.service.MemberService;
import org.example.be.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

// GroupAnnouncementService 공지 삭제 단위 테스트 (Task 4-4)
// 공지 삭제가 실패하는 사유(없는 id · 다른 그룹의 공지 · 비멤버)는 응답이 모두 같아야 한다.
// 사유별로 응답이 갈리면 그룹 멤버가 아닌 사람도 응답만 보고 공지 id 의 존재와 소속 그룹을 알아낼 수 있다.
// 기대값은 ErrorCode 상수가 아니라 문자열 리터럴로 비교한다 - 상수끼리 비교하면 이름이 바뀌어도 통과하는 동어반복이 된다.
@DisplayName("GroupAnnouncementService 공지 삭제 단위 테스트")
@ExtendWith(MockitoExtension.class)
class GroupAnnouncementServiceTest {

	private static final String GROUP_NAME = "우리그룹";
	private static final String OTHER_GROUP_NAME = "남의그룹";
	private static final Long MEMBER_ID = 1L;
	private static final Long ANNOUNCEMENT_ID = 10L;

	// 삭제 경로에서는 쓰지 않지만 서비스 생성자가 요구하므로 함께 주입한다
	@Mock
	private GroupRepository groupRepository;
	@Mock
	private GroupService groupService;
	@Mock
	private GroupAnnouncementRepository groupAnnouncementRepository;
	@Mock
	private MemberService memberService;

	@InjectMocks
	private GroupAnnouncementService groupAnnouncementService;

	@Test
	@DisplayName("그룹 멤버가 아니면 공지를 조회하지도 않고 404 GROUP_ANNOUNCEMENT_NOT_FOUND 이다")
	void deleteGroupAnnouncement_notMember_returnsNotFoundWithoutLookup() {
		when(groupService.isContains(GROUP_NAME, MEMBER_ID)).thenReturn(false);

		Throwable thrown = catchThrowable(() -> groupAnnouncementService.deleteGroupAnnouncement(
			new GroupAnnouncementDeleteReqBody(ANNOUNCEMENT_ID, GROUP_NAME), MEMBER_ID));

		assertAnnouncementNotFound(thrown);
		// 공지를 찾아보는 것 자체가 없어야 응답·처리 경로로 존재 여부가 갈리지 않는다
		verify(groupAnnouncementRepository, never()).findById(any());
	}

	@Test
	@DisplayName("멤버라도 없는 공지 id 면 404 GROUP_ANNOUNCEMENT_NOT_FOUND 이다")
	void deleteGroupAnnouncement_missingId_returnsNotFound() {
		when(groupService.isContains(GROUP_NAME, MEMBER_ID)).thenReturn(true);
		when(groupAnnouncementRepository.findById(ANNOUNCEMENT_ID)).thenReturn(Optional.empty());

		Throwable thrown = catchThrowable(() -> groupAnnouncementService.deleteGroupAnnouncement(
			new GroupAnnouncementDeleteReqBody(ANNOUNCEMENT_ID, GROUP_NAME), MEMBER_ID));

		assertAnnouncementNotFound(thrown);
	}

	@Test
	@DisplayName("다른 그룹의 공지 id 면 403 이 아니라 없는 공지와 같은 404 이고 삭제하지 않는다")
	void deleteGroupAnnouncement_otherGroupsAnnouncement_returnsNotFound() {
		// announcementOf() 안에서도 when() 을 쓰므로, thenReturn() 인자 안에서 호출하면 stubbing 이 중첩되어 실패한다 - 먼저 만들어 둔다
		GroupAnnouncement othersAnnouncement = announcementOf(OTHER_GROUP_NAME);
		when(groupService.isContains(GROUP_NAME, MEMBER_ID)).thenReturn(true);
		when(groupAnnouncementRepository.findById(ANNOUNCEMENT_ID)).thenReturn(Optional.of(othersAnnouncement));

		Throwable thrown = catchThrowable(() -> groupAnnouncementService.deleteGroupAnnouncement(
			new GroupAnnouncementDeleteReqBody(ANNOUNCEMENT_ID, GROUP_NAME), MEMBER_ID));

		assertAnnouncementNotFound(thrown);
		verify(groupAnnouncementRepository, never()).delete(any());
	}

	@Test
	@DisplayName("멤버가 자기 그룹의 공지를 지우면 삭제하고 지운 공지 정보를 돌려준다")
	void deleteGroupAnnouncement_ownGroupsAnnouncement_deletes() {
		GroupAnnouncement announcement = announcementOf(GROUP_NAME);
		when(groupService.isContains(GROUP_NAME, MEMBER_ID)).thenReturn(true);
		when(groupAnnouncementRepository.findById(ANNOUNCEMENT_ID)).thenReturn(Optional.of(announcement));

		GroupAnnouncementDeleteResBody result = groupAnnouncementService.deleteGroupAnnouncement(
			new GroupAnnouncementDeleteReqBody(ANNOUNCEMENT_ID, GROUP_NAME), MEMBER_ID);

		assertThat(result.id()).isEqualTo(ANNOUNCEMENT_ID);
		verify(groupAnnouncementRepository).delete(announcement);
	}

	// 세 가지 실패 사유가 응답에서 구분되지 않는다는 것 = 상태·code·message 가 모두 같다는 것
	private void assertAnnouncementNotFound(Throwable thrown) {
		assertThat(thrown).isInstanceOf(BusinessException.class);
		BusinessException businessException = (BusinessException)thrown;
		assertThat(businessException.getErrorCode().name()).isEqualTo("GROUP_ANNOUNCEMENT_NOT_FOUND");
		assertThat(businessException.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(businessException.getErrorCode().getMessage()).isEqualTo("그룹 공지가 없습니다.");
	}

	private GroupAnnouncement announcementOf(String groupName) {
		// Group 은 생성자가 protected 라 mock 으로 대체한다
		Group group = mock(Group.class);
		when(group.getGroupName()).thenReturn(groupName);

		GroupAnnouncement announcement = new GroupAnnouncement();
		announcement.setId(ANNOUNCEMENT_ID);
		announcement.setGroup(group);
		announcement.setTitle("공지 제목");
		announcement.setContent("공지 내용");
		announcement.setWriterName("작성자");
		return announcement;
	}
}
