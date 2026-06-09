package org.example.be.domain.schedule.dto.request;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.example.be.domain.place.shared.type.PlaceType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

// SchedulePlaceUpdateListReqBody (PUT 래퍼)의 BeanValidation 단위 테스트
// 래퍼의 @NotEmpty(min-1) 와, @Valid를 통한 원소(@NotBlank 등) cascade가 실제로 잘 이뤄지는지 직접 확인한다.
// (증명 테스트의 반전판 - 순수 List 입력 방식일 때에는 무시되던 원소 검증이 래퍼 방식으로 변경 후 schedulePlace[i].xxx 로 잡혀야 한다.)
@DisplayName("SchedulePlaceUpdateListReqBody (PUT 래퍼)의 Bean Validation 단위 테스트")
class SchedulePlaceUpdateListReqBodyTest {

	private static Validator validator;

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 6, 10, 0);

	@BeforeAll
	static void setUp() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		}
	}

	// 모든 필드가 유효한 정상 블록 하나
	private static SchedulePlaceUpdateReqBody validBlock() {
		return new SchedulePlaceUpdateReqBody(1L, "C1", PlaceType.TOURISTSPOT, NOW, NOW.plusHours(1), 1, 1);
	}

	@Test
	@DisplayName("정상 목록이면 위반이 없다")
	void validList_noViolations() {
		SchedulePlaceUpdateListReqBody body = new SchedulePlaceUpdateListReqBody(List.of(validBlock()));

		Set<ConstraintViolation<SchedulePlaceUpdateListReqBody>> violations = validator.validate(body);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("빈 배열이면 위반 (@NotEmpty) - path는 schedulePlaces")
	void emptyList_notEmptyViolation() {
		SchedulePlaceUpdateListReqBody body = new SchedulePlaceUpdateListReqBody(List.of());

		Set<ConstraintViolation<SchedulePlaceUpdateListReqBody>> violations = validator.validate(body);

		// validator의 검증 결과 위반 정보 객체 내 "schedulePlaces" 형태의 텍스트가 포함되어 있는지 확인
		assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("schedulePlaces");
	}

	@Test
	@DisplayName("null 목록이면 위반 (@NotEmpty는 null도 거부해야 함)")
	void nullList_notEmptyViolation() {
		SchedulePlaceUpdateListReqBody body = new SchedulePlaceUpdateListReqBody(null);

		Set<ConstraintViolation<SchedulePlaceUpdateListReqBody>> violations = validator.validate(body);

		assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("schedulePlaces");
	}

	@Test
	@DisplayName("원소 contentId가 공백이면 cascade로 위반이 걸려야 함 - path는 schedulePlaces[0].contentId")
	void blankContentIdInElement_cascadeViolation() {
		// contentId가 공백인 원소를 포함한 리스트를 만든다.
		SchedulePlaceUpdateReqBody invalidBlock = new SchedulePlaceUpdateReqBody(
			1L, " ", PlaceType.TOURISTSPOT, NOW, NOW.plusHours(1), 1, 1);
		SchedulePlaceUpdateListReqBody body = new SchedulePlaceUpdateListReqBody(List.of(invalidBlock));

		Set<ConstraintViolation<SchedulePlaceUpdateListReqBody>> violations = validator.validate(body);

		assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("schedulePlaces[0].contentId");
	}

}
