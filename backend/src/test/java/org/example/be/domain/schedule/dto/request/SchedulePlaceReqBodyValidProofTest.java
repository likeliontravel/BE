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

// [증명] @Valid 는 '맨 List' 의 원소 규칙을 검사하지 않는다 (POST 생성 DTO contentId @NotBlank 예시)
// POST /schedule/detail/{scheduleId} 의 @Valid @RequestBody List<SchedulePlaceReqBody> 가
// 실제로 타는 경로(=List 를 통째로 검증)를 그대로 재현한다.
@DisplayName("[증명] @Valid는 Controller에서 입력받은 List<>에 대해 원소 규칙을 검사하지 않고 있다(POST 생성 DTO contentId @NotBlank)")
class SchedulePlaceReqBodyValidProofTest {
	private static Validator validator;
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 6, 10, 0);

	@BeforeAll
	static void setUp() {
		// 컨트롤러의 @Valid가 내부적으로 사용하는 바로 그 jakarta Validator
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		}
	}

	@Test
	@DisplayName("contentId 공백 블록: 단독 검증은 위반을 잡지만, List로 감싸면 위반 0건이다.")
	void blankContentId_caughtAlone_butIgnoredInsideList() {
		// given: contentId가 공백(" ")인 '동일한'불량 생성 블록 하나
		SchedulePlaceReqBody invalidBlock = new SchedulePlaceReqBody(
			" ", PlaceType.TOURISTSPOT, NOW, NOW.plusHours(1), 1, 1);

		// 이 블록을 '원소 자체'로 검증하면 @NotBlank가 작동해 contentId 위반이 잡힌다.(규칙은 옳게 선언되어 있음)
		Set<ConstraintViolation<SchedulePlaceReqBody>> whenValidatedAlone = validator.validate(invalidBlock);
		assertThat(whenValidatedAlone)
			.as("블록에 직접 validator가 닿도록 하면 @NotBlank가 작동해서 contentId 위반이 잡힌다.")
			.extracting(violation -> violation.getPropertyPath().toString())
			.contains("contentId");

		// 현재 컨트롤러에서처럼 List<> 로 감싸서 검증하면 -> 위반이 안걸리고 있다.
		// @Valid가 List 내부 원소까지 내려가지를 않아서 그 안 원소의 필드인 contentId의 @NotBlank는 호출조차 되지 않는다.
		List<SchedulePlaceReqBody> body = List.of(invalidBlock);
		assertThat(validator.validate(body))
			.as("List를 통째로 검증시키면 원소의 @NotBlank까지 @Valid (validator)가 닿지 않아 위반이 일어나지 않는다.(현재 생성 메서드 방식의 실태)")
			.isEmpty();
	}
}
