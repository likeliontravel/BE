package org.example.be.mail.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.example.be.exception.custom.EmailAlreadyRegisteredException;
import org.example.be.mail.domain.Mail;
import org.example.be.mail.dto.MailVerifyReqBody;
import org.example.be.mail.repository.MailRepository;
import org.example.be.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

	private final JavaMailSender mailSender;
	private final MailRepository mailRepository;
	private final MemberRepository memberRepository;

	@Value("${spring.mail.username}")
	private String fromEmail;

	private static final int CODE_EXPIRATION_MINUTES = 5; // 인중 코드 유효시간 (5분)

	// 인증 코드 보내는 로직
	@Transactional
	public void sendMail(String email) {
		if (memberRepository.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException("이미 가입된 이메일입니다.");
		}

		String verificationCode = generateVerificationCode();

		try {

			Mail mail = mailRepository.findByEmail(email).orElse(new Mail());

			mail.setEmail(email);
			mail.setAuthCode(verificationCode);
			mail.setCreatedAt(LocalDateTime.now());
			mail.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));

			mailRepository.save(mail);

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
	@Transactional
	public boolean verifyCode(MailVerifyReqBody mailVerifyReqBody) {

		Optional<Mail> mailOptional = mailRepository.findByEmail(mailVerifyReqBody.email());

		if (mailOptional.isPresent()) {

			Mail mail = mailOptional.get();

			// 코드 유효성 검증
			if (mail.getExpiresAt().isBefore(LocalDateTime.now())) {

				mailRepository.deleteByEmail(mail.getEmail()); // 만료된 코드 삭제

				throw new RuntimeException("인증 코드가 만료 되었습니다.");
			}

			if (!mail.getAuthCode().equals(mailVerifyReqBody.code())) {

				throw new RuntimeException("인증코드가 다릅니다.");
			}

			// 인증 성공 시 데이터 삭제
			mailRepository.deleteByEmail(mail.getEmail());

			return true;

		} else {

			throw new RuntimeException("인증 코드를 찾을 수 없습니다.");
		}

	}

	// 인증 코드 만드는 로직
	private static String generateVerificationCode() {

		Random random = new Random();

		int code = 100000 + random.nextInt(900000); // 6자리 랜덤 코드

		return String.valueOf(code);
	}
}