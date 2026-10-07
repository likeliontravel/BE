package org.example.be.global.exception.code;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

// ErrorCode 구현체 계약 테스트.
// ErrorCode 가 인터페이스가 되면서(Task 4-1) 컴파일러가 더 이상 지켜 주지 않는 두 가지를 여기서 강제한다.
//   ① 구현체는 반드시 enum 이다 - 일반 클래스는 name() 을 임의로 구현할 수 있어 CommonResponse.code 계약이 깨진다.
//   ② 상수 이름은 모든 구현체를 통틀어 유일하다 - 단일 enum 일 때는 컴파일러가 막았지만, 여러 파일로 흩어진 뒤로는 아무도 막지 않는다.
// 여기에 더해, 이미 응답으로 나가고 있는 code 66개가 사라지거나 이름이 바뀌지 않았는지를 문자열 리터럴로 고정한다.
//
// 구현체는 각 도메인 패키지의 exception/ 아래에 흩어져 있으므로 org.example.be 전체를 클래스패스 스캔해서 찾는다.
// 스프링 컨텍스트는 띄우지 않는다 - 스캐너는 클래스 파일의 메타데이터만 읽는다.
@DisplayName("ErrorCode 구현체 계약 테스트")
class ErrorCodeContractTest {

	private static final String BASE_PACKAGE = "org.example.be";

	private static final int EXPECTED_TOTAL_CONSTANTS = 66;

	// Phase 4 이전의 단일 enum(커밋 e540d6f~1 의 ErrorCode.java)에서 옮겨 적은 66개다.
	// 현재 구현체에서 뽑아 오면 이름이 바뀌어도 자기 자신과 비교하게 되므로, 반드시 리터럴로 둔다.
	// 상수를 삭제하거나 이름을 바꾸면 프론트가 받는 code 가 바뀌는 계약 변경이다.
	// 의도한 변경이라면 CONTRACT-CHANGES.md 에 기록한 뒤 여기서도 함께 고친다.
	private static final Set<String> KNOWN_CODES = Set.of(
		// CommonErrorCode (19)
		"BAD_REQUEST", "INTERNAL_SERVER_ERROR", "INVALID_URI_VARIABLES",
		"INVALID_REQUEST_BODY", "MISSING_REQUIRED_PARAMETER", "MISSING_REQUIRED_HEADER", "METHOD_NOT_ALLOWED",
		"ENDPOINT_NOT_FOUND", "DATA_INTEGRITY_VIOLATION", "MISSING_REQUIRED_PART", "INVALID_MULTIPART_REQUEST",
		"FILE_SIZE_EXCEEDED", "UNSUPPORTED_MEDIA_TYPE",
		"UNAUTHORIZED", "FORBIDDEN",
		"RESOURCE_CREATION_FAILED", "RESOURCE_UPDATE_FAILED", "RESOURCE_DELETE_FAILED",
		"EXTERNAL_API_FAILED",
		// MemberErrorCode (7)
		"EMAIL_ALREADY_REGISTERED", "EMAIL_NOT_REGISTERED", "MEMBER_NOT_FOUND",
		"LOGIN_FAILED", "SOCIAL_ACCOUNT_LOGIN_REQUIRED", "INVALID_TOKEN", "INVALID_REFRESH_TOKEN",
		// MailErrorCode (3)
		"MAIL_SEND_FAILED", "MAIL_CODE_EXPIRED", "MAIL_CODE_MISMATCH",
		// GroupErrorCode (10)
		"GROUP_NOT_FOUND", "GROUP_NAME_ALREADY_EXIST", "GROUP_NOT_CREATOR", "GROUP_ACCESS_DENIED",
		"GROUP_ALREADY_MEMBER", "GROUP_CREATOR_CANNOT_EXIT",
		"INVALID_INVITATION", "INVITATION_EXPIRED", "INVITATION_NOT_FOUND",
		"GROUP_ANNOUNCEMENT_NOT_FOUND",
		// BoardErrorCode (10)
		"BOARD_NOT_FOUND", "COMMENT_NOT_FOUND", "BOARD_NOT_WRITER", "COMMENT_NOT_WRITER",
		"INVALID_PARENT_COMMENT_OF_BOARD", "INVALID_PARENT_COMMENT",
		"BOARD_TITLE_BLANK", "BOARD_CONTENT_BLANK", "BOARD_IMAGE_COUNT_EXCEEDED", "BOARD_IMAGE_EMPTY",
		// ScheduleErrorCode (6)
		"SCHEDULE_NOT_FOUND", "SCHEDULE_ALREADY_EXIST", "SCHEDULE_PLACE_NOT_FOUND",
		"SCHEDULE_PLACE_DUPLICATE_ORDER", "SCHEDULE_PLACE_DUPLICATE_ID", "SCHEDULE_INVALID_PERIOD",
		// PlaceErrorCode (3)
		"PLACE_NOT_FOUND", "INVALID_REGION", "INVALID_THEME",
		// NotificationErrorCode (3)
		"NOTIFICATION_NOT_FOUND", "NOTIFICATION_FORBIDDEN", "NOTIFICATION_SEND_FAILED",
		// FileErrorCode (5)
		"GCS_UPLOAD_FAILED", "GCS_DELETE_FAILED",
		"INVALID_IMAGE_FILE_TYPE", "INVALID_VIDEO_FILE_TYPE", "INVALID_RECORD_FILE_TYPE"
	);

	@Test
	@DisplayName("모든 ErrorCode 구현체는 enum 이다")
	void allImplementationsAreEnums() {
		List<Class<?>> implementations = findImplementations();

		// 스캔이 아무것도 못 찾으면 아래 단언이 '위반 0건'으로 통과해 버리므로 먼저 막는다.
		assertThat(implementations)
			.as("ErrorCode 구현체를 하나도 찾지 못했습니다 - 스캔 범위(%s)를 확인하세요", BASE_PACKAGE)
			.isNotEmpty();

		List<String> nonEnumImplementations = implementations.stream()
			.filter(implementation -> !implementation.isEnum())
			.map(Class::getName)
			.toList();

		assertThat(nonEnumImplementations)
			.as("enum 이 아닌 ErrorCode 구현체가 있습니다 - 일반 클래스는 name() 을 임의로 구현할 수 있어 code 계약이 깨집니다")
			.isEmpty();
	}

	@Test
	@DisplayName("code(상수 이름)는 모든 구현체를 통틀어 유일하다")
	void codeNamesAreGloballyUnique() {
		// code -> 그 이름을 가진 enum 들. 정렬해 두면 실패 메시지를 읽기 쉽다.
		Map<String, List<String>> ownersByCode = new TreeMap<>();
		for (ErrorCode errorCode : allErrorCodes()) {
			String owner = ((Enum<?>)errorCode).getDeclaringClass().getSimpleName();
			ownersByCode.computeIfAbsent(errorCode.name(), code -> new ArrayList<>()).add(owner);
		}

		Map<String, List<String>> duplicates = ownersByCode.entrySet().stream()
			.filter(entry -> entry.getValue().size() > 1)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		assertThat(duplicates)
			.as("같은 code 가 여러 구현체에 있습니다 (code=[소속 enum]) - 프론트가 code 로 분기할 수 없게 됩니다")
			.isEmpty();
	}

	@Test
	@DisplayName("이미 응답으로 나간 code 66개는 사라지거나 이름이 바뀌지 않는다")
	void knownCodesAreNeverRemovedOrRenamed() {
		assertThat(allCodeNames())
			.as("기존 code 가 사라졌습니다 - 삭제·개명은 프론트 계약 변경입니다 (의도했다면 CONTRACT-CHANGES.md 기록 후 KNOWN_CODES 갱신)")
			.containsAll(KNOWN_CODES);
	}

	@Test
	@DisplayName("ErrorCode 상수는 모두 66개다")
	void totalConstantCountIs66() {
		assertThat(allCodeNames())
			.as("상수 개수가 바뀌었습니다 - 의도한 추가·삭제라면 EXPECTED_TOTAL_CONSTANTS 와 KNOWN_CODES 를 함께 갱신하세요")
			.hasSize(EXPECTED_TOTAL_CONSTANTS);
	}

	// org.example.be 전체에서 ErrorCode 를 구현한 클래스를 찾는다.
	private List<Class<?>> findImplementations() {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
			// 기본 판정은 '독립적인 구체 클래스'만 후보로 삼아서, enum 이 아닌 추상 클래스·내부 클래스 구현체를 놓친다.
			// 인터페이스(ErrorCode 자신 포함)만 빼고 전부 후보로 넣는다.
			@Override
			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
				return !beanDefinition.getMetadata().isInterface();
			}
		};
		scanner.addIncludeFilter(new AssignableTypeFilter(ErrorCode.class));

		List<Class<?>> implementations = new ArrayList<>();
		for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
			Class<?> implementation = ClassUtils.resolveClassName(
				String.valueOf(candidate.getBeanClassName()), getClass().getClassLoader());

			// enum 상수에 본문({ ... })이 붙으면 컴파일러가 그 enum 을 상속한 익명 클래스(X$1)를 만든다.
			// 이 클래스는 isEnum() 이 false 라서 그대로 두면 정상 enum 을 위반으로 오판한다. 소속 enum 이 이미 검사되므로 건너뛴다.
			if (isEnumConstantBody(implementation)) {
				continue;
			}
			implementations.add(implementation);
		}
		return implementations;
	}

	private boolean isEnumConstantBody(Class<?> type) {
		return type.isAnonymousClass() && type.getSuperclass() != null && type.getSuperclass().isEnum();
	}

	private List<ErrorCode> allErrorCodes() {
		List<ErrorCode> errorCodes = new ArrayList<>();
		for (Class<?> implementation : findImplementations()) {
			// enum 이 아닌 구현체는 첫 번째 테스트가 실패시킨다. 여기서는 enum 상수만 모은다.
			if (!implementation.isEnum()) {
				continue;
			}
			for (Object constant : implementation.getEnumConstants()) {
				errorCodes.add(ErrorCode.class.cast(constant));
			}
		}
		return errorCodes;
	}

	// 중복 이름도 그대로 센다(Set 이 아니라 List) - 개수 테스트가 중복 정의를 함께 잡도록.
	private List<String> allCodeNames() {
		return allErrorCodes().stream()
			.map(ErrorCode::name)
			.toList();
	}
}
