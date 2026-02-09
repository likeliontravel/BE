package org.example.be.mail.controller;

import org.example.be.mail.dto.MailSendReqBody;
import org.example.be.mail.dto.MailVerifyReqBody;
import org.example.be.mail.service.MailService;
import org.example.be.response.CommonResponse;
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
	@PostMapping("/send")
	public ResponseEntity<CommonResponse<String>> mailSend(@Valid @RequestBody MailSendReqBody reqBody) {

		try {

			mailService.sendMail(reqBody.email());

			return ResponseEntity.status(HttpStatus.OK)
				.body(CommonResponse.success(null, "이메일 코드 받기 성공 : " + reqBody.email()));

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
		}
	}

	// 메일 코드 인증
	@PostMapping("/verify")
	public ResponseEntity<CommonResponse<String>> mailVerify(@Valid @RequestBody MailVerifyReqBody reqBody) {

		try {

			boolean isVerified = mailService.verifyCode(reqBody);

			if (isVerified) {

				return ResponseEntity.status(HttpStatus.OK)
					.body(CommonResponse.success(reqBody.email(), "이메일 인증 성공"));
			} else {

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.error(400, "이메일 인증 실패"));
			}
		} catch (RuntimeException e) {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.error(400, e.getMessage()));
		}
	}
}
