package org.example.be.domain.mail.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.example.be.domain.mail.dto.MailVerifyReqBody;
import org.example.be.domain.member.repository.MemberRepository;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

	private final JavaMailSender mailSender;
	private final MemberRepository memberRepository;
	private final StringRedisTemplate stringRedisTemplate;

	@Value("${spring.mail.username}")
	private String fromEmail;

	private static final int CODE_EXPIRATION_MINUTES = 5; // 인증 코드 유효시간 (5분)

	// 인증 코드 보내는 로직
	public void sendMail(String email) {
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED, "입력된 이메일: " + email);
		}

		String verificationCode = generateVerificationCode();

		try {
			stringRedisTemplate.opsForValue().set(email, verificationCode, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);

			// 이메일 전송
			SimpleMailMessage message = new SimpleMailMessage();

			message.setFrom(fromEmail);

			message.setTo(email);
			message.setSubject("이메일 인증 코드 요청");
			message.setText("요청하신 이메일 인증 코드는 : " + verificationCode + " 입니다.\n" +
				"\n" +
				"인증 코드 유효시간은 5분 입니다.");

			mailSender.send(message);

		} catch (Exception e) {
			// Redis, SMTP라는 외부 경계의 실패. 우리가 통제할 수 없으므로 도메인 예외로 옮긴다.
			// cause(e)를 넘겨서 로그에 진짜 원인(연결 타임아웃, 인증 실패 등)이 변환 지점에서 소멸하지 않도록 한다.
			throw new BusinessException(ErrorCode.MAIL_SEND_FAILED, "인증 메일 발송 실패 - email: " + email, e);
		}
	}

	// 인증 코드 검사하는 로직. 실패는 예외로만 표현한다(반환값으로 실패를 알리지 않는다).
	public void verifyCode(MailVerifyReqBody mailVerifyReqBody) {

		String storedCode = stringRedisTemplate.opsForValue().get(mailVerifyReqBody.email());

		if (storedCode == null) {
			throw new BusinessException(ErrorCode.MAIL_CODE_EXPIRED, "email: " + mailVerifyReqBody.email());
		}

		if (!storedCode.equals(mailVerifyReqBody.code())) {
			throw new BusinessException(ErrorCode.MAIL_CODE_MISMATCH, "email: " + mailVerifyReqBody.email());
		}

		stringRedisTemplate.delete(mailVerifyReqBody.email()); // 인증 성공 시 Redis에서 삭제
	}

	// 인증 코드 만드는 로직
	private static String generateVerificationCode() {

		Random random = new Random();

		int code = 100000 + random.nextInt(900000); // 6자리 랜덤 코드

		return String.valueOf(code);
	}

	public void sendPasswordResetMail(String email) {
		if (!memberRepository.existsByEmail(email)) {
			/// TODO: EmailAlreadyRegisteredException이 이미 가입된 이메일인 경우에도, 가입되지 않은 이메일인 경우에도 사용되고 있었음. 해당 사항 PR에 안내.
			throw new BusinessException(ErrorCode.EMAIL_NOT_REGISTERED, "입력된 이메일: " + email);
		}
		String verificationCode = generateVerificationCode();
		try {
			stringRedisTemplate.opsForValue().set(email, verificationCode, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail);
			message.setTo(email);
			message.setSubject("비밀번호 재설정 인증 코드 요청");
			message.setText("요청하신 비밀번호 재설정 인증 코드는 : " + verificationCode + " 입니다.\n" +
				"\n" +
				"인증 코드 유효시간은 5분 입니다.");
			mailSender.send(message);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.MAIL_SEND_FAILED, "재설정 메일 발송 실패 - email: " + email, e);
		}
	}
}
