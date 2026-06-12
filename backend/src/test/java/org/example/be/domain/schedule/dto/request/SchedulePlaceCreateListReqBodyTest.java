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

// SchedulePlaceCreateListReqBody (POST 래퍼)의 Bean Validation 단위 테스트.
// PUT 래퍼와 동일하게 @NotEmpty(min-1) + @Valid(원소 cascade)를 검증한다.
// 단, @Valid 를 통한 원소 cascade 검증은 PUT 과 동일하게 살아 있어야 한다(schedulePlaces[i].xxx).
@DisplayName("SchedulePlaceCreateListReqBody (POST 래퍼)의 Bean Validation 단위 테스트")
class SchedulePlaceCreateListReqBodyTest {

	private static Validator validator;

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 6, 10, 0);

	@BeforeAll
	static void setUp() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		}
	}

	// 모든 필드가 유효한 정상 생성 블록 하나 (POST 원소는 schedulePlaceId 가 없어 6인자)
	private static SchedulePlaceReqBody validBlock() {
		return new SchedulePlaceReqBody("C1", PlaceType.TOURISTSPOT, NOW, NOW.plusHours(1), 1, 1);
	}

	@Test
	@DisplayName("정상 목록이면 위반이 없다")
	void validList_noViolations() {
		SchedulePlaceCreateListReqBody body = new SchedulePlaceCreateListReqBody(List.of(validBlock()));

		Set<ConstraintViolation<SchedulePlaceCreateListReqBody>> violations = validator.validate(body);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("빈 배열이어도 위반이 없다 (@NotEmpty 미부착 → no-op 허용, PUT 과 정반대)")
	void emptyList_notEmptyViolations() {
		SchedulePlaceCreateListReqBody body = new SchedulePlaceCreateListReqBody(List.of());

		Set<ConstraintViolation<SchedulePlaceCreateListReqBody>> violations = validator.validate(body);

		assertThat(violations)
			.extracting(v -> v.getPropertyPath().toString())
			.contains("schedulePlaces");
	}

	@Test
	@DisplayName("null 목록이면 위반 (@NotEmpty는 null도 거부해야 함)")
	void nullList_notEmptyViolation() {
		SchedulePlaceCreateListReqBody body = new SchedulePlaceCreateListReqBody(null);

		Set<ConstraintViolation<SchedulePlaceCreateListReqBody>> violations = validator.validate(body);

		assertThat(violations)
			.extracting(v -> v.getPropertyPath().toString())
			.contains("schedulePlaces");
	}

	@Test
	@DisplayName("원소 contentId 가 공백이면 cascade 로 위반 — path 는 schedulePlaces[0].contentId")
	void blankContentIdInElement_cascadeViolation() {
		SchedulePlaceReqBody invalidBlock = new SchedulePlaceReqBody(
			" ", PlaceType.TOURISTSPOT, NOW, NOW.plusHours(1), 1, 1);
		SchedulePlaceCreateListReqBody body = new SchedulePlaceCreateListReqBody(List.of(invalidBlock));

		Set<ConstraintViolation<SchedulePlaceCreateListReqBody>> violations = validator.validate(body);

		assertThat(violations)
			.extracting(v -> v.getPropertyPath().toString())
			.contains("schedulePlaces[0].contentId");
	}
}