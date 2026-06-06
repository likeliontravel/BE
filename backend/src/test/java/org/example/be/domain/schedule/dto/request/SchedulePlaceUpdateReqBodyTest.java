package org.example.be.domain.schedule.dto.request;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;

import org.example.be.domain.place.shared.type.PlaceType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

// SchedulePlaceUpdateReqBody의 Bean Validation 제약 (@NotBlank / @NotNull / @Min) 단위 테스트
// 컨트롤러의 @Valid가 실제로 사용하는 jakarta Validator를 직접 호출해 위반 발생 여부를 검증한다.
@DisplayName("SchedulePlaceUpdateReqBody Bean Validation 단위 테스트")
class SchedulePlaceUpdateReqBodyTest {

	private static Validator validator;

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 6, 10, 0);

	@BeforeAll
	static void setUp() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		}
	}

	@Test
	@DisplayName("모든 필드가 유효하면 위반이 없다")
	void valid_noViolations() {
		SchedulePlaceUpdateReqBody reqBody = new SchedulePlaceUpdateReqBody(
			1L, "C1", PlaceType.TOURISTSPOT, NOW, NOW.plusHours(1), 1, 1);

		Set<ConstraintViolation<SchedulePlaceUpdateReqBody>> violations = validator.validate(reqBody);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("schedulePlaceId가 null이어도 유효하다 (신규 블록)")
	void nullSchedulePlaceId_isValid() {
		SchedulePlaceUpdateReqBody reqBody = new SchedulePlaceUpdateReqBody(
			null, "C1", PlaceType.TOURISTSPOT, NOW, NOW.plusHours(1), 1, 1);

		Set<ConstraintViolation<SchedulePlaceUpdateReqBody>> violations = validator.validate(reqBody);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("contentId가 공백이면 위반(@NotBlank)")
	void blankContentId_violation() {
		SchedulePlaceUpdateReqBody reqBody = new SchedulePlaceUpdateReqBody(
			1L, " ", PlaceType.TOURISTSPOT, NOW, NOW.plusHours(1), 1, 1
		);

		Set<ConstraintViolation<SchedulePlaceUpdateReqBody>> violations = validator.validate(reqBody);

		assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("contentId");
	}

	@Test
	@DisplayName("placeType이 null이면 위반 (@NotNull)")
	void nullPlaceType_violations() {
		SchedulePlaceUpdateReqBody reqBody = new SchedulePlaceUpdateReqBody(
			1L, "C1", null, NOW, NOW.plusHours(1), 1, 1
		);
		Set<ConstraintViolation<SchedulePlaceUpdateReqBody>> violations = validator.validate(reqBody);
		assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("placeType");
	}

	@Test
	@DisplayName("방문 시작/종료 시간이 null이면 각각 위반 (@NotNull)")
	void nullVisitTimes_violations() {
		SchedulePlaceUpdateReqBody reqBody = new SchedulePlaceUpdateReqBody(
			1L, "C1", PlaceType.TOURISTSPOT, null, null, 1, 1);

		Set<ConstraintViolation<SchedulePlaceUpdateReqBody>> violations = validator.validate(reqBody);

		assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("visitStart", "visitedEnd");
	}

	@Test
	@DisplayName("dayOrder가 1 미만이면 위반 (@Min)")
	void dayOrder_violation() {
		SchedulePlaceUpdateReqBody reqBody = new SchedulePlaceUpdateReqBody(
			1L, "C1", PlaceType.TOURISTSPOT, NOW, NOW.plusHours(1), 0, 1);

		Set<ConstraintViolation<SchedulePlaceUpdateReqBody>> violations = validator.validate(reqBody);

		assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("dayOrder");
	}

	@Test
	@DisplayName("orderInDay가 null이면 위반 (@NotNull)")
	void nullOrderInDay_violation() {
		SchedulePlaceUpdateReqBody reqBody = new SchedulePlaceUpdateReqBody(
			1L, "C1", PlaceType.TOURISTSPOT, NOW, NOW.plusHours(1), 1, null);

		Set<ConstraintViolation<SchedulePlaceUpdateReqBody>> violations = validator.validate(reqBody);
		assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("orderInDay");

	}

}
