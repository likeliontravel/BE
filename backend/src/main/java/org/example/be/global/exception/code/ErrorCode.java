package org.example.be.global.exception.code;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 계약.
 *
 * 도메인별 구현체 enum 이 이 인터페이스를 구현한다.
 * BusinessException, advice, ErrorResponseWriter 는 이 타입만 알면 되므로, 도메인이 늘어도 시그니처가 바뀌지 않는다.
 *
 * 구현체 배치 - 그 상수가 표현하는 규칙을 소유한 패키지의 exception/ 아래에 둔다.
 * 		global/exception/code/CommonErrorCode 				<- 공통, 프레임워크 요청 오류, 인증/인가, 도메인 특정이 없는 리소스의 CRUD, 외부 연동
 * 		domain/member/exception/MemberErrorCode				<- 회원, 인증 토큰
 * 		domain/mail/exception/MailErrorCode					<- 인증 메일
 * 		domain/group/exception/GroupErrorCode				<- 그룹, 초대, 공지
 * 		domain/board/exception/BoardErrorCode				<- 게시글, 댓글
 * 		domain/schedule/exception/ScheduleErrorCode			<- 일정, 세부 일정
 * 		domain/place/exception/PlaceErrorCode				<- 장소, 지역, 테마
 * 		domain/notification/exception/NotificationErrorCode <- 알림
 * 		storage/gcs/exception/FileErrorCode					<- 파일 스토리지
 *
 * 	global 패키지가 던지는 상수는 CommonErrorCode 에 둔다 - global 이 domain 에 의존하는 역방향을 만들지 않기 위해서다.
 * 	새 도메인에 상수가 필요해지면 같은 형태의 enum 을 그 도메인의 exception/ 아래에 하나 더 만든다.
 */
public interface ErrorCode {

	HttpStatus getStatus();

	String getMessage();

	/**
	 * 에러 식별자. 응답의 CommonResponse.code 로 그대로 나가는 공개 계약이다.
	 * enum 은 Enum.name() (public final) 으로 자동 충족한다.
	 *
	 * 구현체는 반드시 enum 이어야 한다 - 일반 클래스는 name() 을 임의로 구현할 수 있어 code 계약이 깨진다.
	 * 상수 이름은 모든 구현체를 통틀어 유일해야 한다 - code 가 겹치면 프론트 분기가 성립하지 않는다.
	 * ErrorCodeContractTest 가 두 조건을 강제한다.
	 */
	String name();
}
