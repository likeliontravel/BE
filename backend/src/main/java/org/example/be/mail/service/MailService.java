package org.example.be.mail.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.example.be.exception.custom.EmailAlreadyRegisteredException;
import org.example.be.mail.dto.MailVerifyReqBody;
import org.example.be.member.repository.MemberRepository;
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
			throw new EmailAlreadyRegisteredException("이미 가입된 이메일입니다.");
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

			throw new RuntimeException(e);
		}
	}

	// 인증 코드 검사하는 로직
	public boolean verifyCode(MailVerifyReqBody mailVerifyReqBody) {

		String storedCode = stringRedisTemplate.opsForValue().get(mailVerifyReqBody.email());

		if (storedCode == null) {
			throw new RuntimeException("인증 코드를 찾을 수 없거나 만료 되었습니다.");
		}

		if (!storedCode.equals(mailVerifyReqBody.code())) {
			throw new RuntimeException("인증코드가 다릅니다.");
		}

		stringRedisTemplate.delete(mailVerifyReqBody.email()); // 인증 성공 시 Redis에서 삭제
		return true;
	}

	// 인증 코드 만드는 로직
	private static String generateVerificationCode() {

		Random random = new Random();

		int code = 100000 + random.nextInt(900000); // 6자리 랜덤 코드

		return String.valueOf(code);
	}
}
