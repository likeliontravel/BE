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
// POST /schedule/detail/{scheduleId} 가 '예전에' 베어 List(@Valid @RequestBody List<SchedulePlaceReqBody>)로
// 받던 시절, 원소 검증이 왜 샜는지를 코드로 증언하기 위해 '보존'하는 테스트다.
//
// ※ 주의: 이 테스트는 래퍼 전환(현재 코드) 후에도 계속 GREEN 이다 — 의도된 상태다.
//   컨트롤러를 거치지 않고 validator.validate(List<>) 를 '직접' 호출하는 순수 단위 테스트라,
//   우리가 바꾼 것(컨트롤러 파라미터 List→래퍼)과 무관하게 "베어 List 는 원소를 순회하지 않는다"는
//   Bean Validation 스펙만을 보여준다. 즉 GREEN = "(규칙은 옳은데) 베어 List 경로에선 검증이 누락된다"는
//   증거이지, 현재 코드가 버그라는 뜻이 아니다.
//   현재 코드의 정방향 보장(래퍼는 원소까지 cascade 됨)은 SchedulePlaceCreateListReqBodyTest 가 맡는다.
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
