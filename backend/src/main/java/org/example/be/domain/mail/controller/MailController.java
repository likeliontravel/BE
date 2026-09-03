package org.example.be.domain.mail.controller;

import org.example.be.domain.mail.dto.MailSendReqBody;
import org.example.be.domain.mail.dto.MailVerifyReqBody;
import org.example.be.domain.mail.service.MailService;
import org.example.be.global.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailController {

	private final MailService mailService;

	// 메일 인증 코드 발급 받기
	//
	// try-catch를 두지 않는다. 실패는 MailService가 BusinessException으로 던지고
	// GlobalExceptionHandler가 ErrorCode의 상태, 문구로 규격 응답을 만든다
	// 여기서 잡으면 문제 세가지
	// 1. 상태코드가 임의로 뭉개짐 (가입된 이메일이 400이 아닌 500으로 나감)
	// 2. 예외 원문 (SMTP, Redis 연결 문자열)이 그대로 응답에 실림
	// 3. advice에 도달하지 못해 로그조차 남지 않음. /mail/**은 permitAll
	@PostMapping("/send")
	public ResponseEntity<CommonResponse<String>> mailSend(@Valid @RequestBody MailSendReqBody reqBody) {

		mailService.sendMail(reqBody.email());

		return ResponseEntity.status(HttpStatus.OK)
			.body(CommonResponse.success(null, "이메일 코드 받기 성공 : " + reqBody.email()));

	}

	// 메일 코드 인증
	@PostMapping("/verify")
	public ResponseEntity<CommonResponse<String>> mailVerify(@Valid @RequestBody MailVerifyReqBody reqBody) {

		// 실패는 예외로만 표현되므로 반환값을 분기할 필요가 없다.
		mailService.verifyCode(reqBody);

		return ResponseEntity.ok(CommonResponse.success(reqBody.email(), "이메일 인증 성공"));
	}
}
