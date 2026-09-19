package org.example.be.domain.mail.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.be.domain.member.repository.MemberRepository;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.domain.mail.dto.MailVerifyReqBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

	private final JavaMailSender mailSender;
	private final MemberRepository memberRepository;
	private final StringRedisTemplate stringRedisTemplate;

	@Value("${app.mail.from}")
	private String fromEmail;
	@Value("${gcs.bucket.mail}")
	private String mailImageUrl;

	private static final int CODE_EXPIRATION_MINUTES = 5; // 인증 코드 유효시간 (5분)

	// 인증 코드 보내는 로직
	public void sendMail(String email) {
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED, "입력된 이메일: " + email);
		}

		String verificationCode = generateVerificationCode();

		stringRedisTemplate.opsForValue().set(email, verificationCode, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);

		String html = buildVerificationHtml(
				"이메일 인증번호",
				"안녕하세요, 투리브입니다.<br>아래 인증번호를 입력해 이메일 인증을 완료해 주세요.",
				verificationCode
		);

		sendHtmlMail(email, "[투리브] 이메일 인증번호입니다", html);
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

	public void sendPasswordResetMail(String email) {
		if (!memberRepository.existsByEmail(email)) {
			/// TODO: EmailAlreadyRegisteredException이 이미 가입된 이메일인 경우에도, 가입되지 않은 이메일인 경우에도 사용되고 있었음. 해당 사항 PR에 안내.
			throw new BusinessException(ErrorCode.EMAIL_NOT_REGISTERED, "입력된 이메일: " + email);
		}
		String verificationCode = generateVerificationCode();

		stringRedisTemplate.opsForValue().set(email, verificationCode, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);

		String html = buildVerificationHtml(
				"비밀번호 재설정 인증번호",
				"안녕하세요, 투리브입니다.<br>아래 인증번호를 입력해 비밀번호 재설정을 진행해 주세요.",
				verificationCode
		);

		sendHtmlMail(email, "[투리브] 비밀번호 재설정 인증번호입니다", html);
	}

	// 공통으로 쓸 HTML 빌더 (인증코드 박스 부분만 재사용)
	private String buildVerificationHtml(String title, String description, String code) {
		String imageUrl = mailImageUrl;

		return """
    <!DOCTYPE html>
    <html lang="ko">
    <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="color-scheme" content="light dark">
    <meta name="supported-color-schemes" content="light dark">
    <style>
      :root { color-scheme: light dark; supported-color-schemes: light dark; }
      .card { background-color:#ffffff !important; }
      .code-box { background-color:#f0f3f6 !important; }
      .body-text { color:#333333 !important; }
      .sub-text { color:#aaaaaa !important; }
      .footer { background-color:#fafafa !important; color:#bbbbbb !important; }
      .code-label { color:#8a939c !important; }
      .code-value { color:#2f7bf6 !important; }
      .outer-bg { background-color:#eef1f4 !important; }
      @media (prefers-color-scheme: dark) {
        .card { background-color:#ffffff !important; }
        .code-box { background-color:#f0f3f6 !important; }
        .body-text { color:#333333 !important; }
        .sub-text { color:#aaaaaa !important; }
        .footer { background-color:#fafafa !important; color:#bbbbbb !important; }
        .code-label { color:#8a939c !important; }
        .code-value { color:#2f7bf6 !important; }
        .outer-bg { background-color:#eef1f4 !important; }
      }
      img { max-width:100%%; height:auto; }
    </style>
    </head>
    <body style="margin:0; padding:0; background-color:#eef1f4;">
    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" bgcolor="#eef1f4" class="outer-bg" style="background-color:#eef1f4; padding:40px 0; font-family:'Apple SD Gothic Neo','Malgun Gothic',Arial,sans-serif;">
      <tr><td align="center">
        <table role="presentation" width="480" cellpadding="0" cellspacing="0" bgcolor="#ffffff" class="card" style="background-color:#ffffff; border-radius:16px; overflow:hidden; max-width:480px; width:100%%;">
          <tr>
            <td align="center" style="padding:40px 32px 0 32px;">
              <img src="%s" width="220" height="93" alt="투리브 캐릭터" style="display:block; width:220px; height:auto; max-width:60%%;">
            </td>
          </tr>
          <tr>
            <td align="center" style="padding:0 32px 8px 32px;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" bgcolor="#f0f3f6" class="code-box" style="background-color:#f0f3f6; border-radius:12px;">
                <tr><td align="center" class="code-label" style="padding:24px 20px 8px 20px; font-size:14px; color:#8a939c;">%s</td></tr>
                <tr><td align="center" class="code-value" style="padding:0 20px 24px 20px; font-size:34px; font-weight:700; color:#2f7bf6; letter-spacing:6px;">%s</td></tr>
              </table>
            </td>
          </tr>
          <tr>
            <td align="center" class="body-text" style="padding:20px 32px 8px 32px; font-size:14px; color:#333333; line-height:1.6;">
              %s
            </td>
          </tr>
          <tr>
            <td align="center" class="sub-text" style="padding:0 32px 32px 32px; font-size:12px; color:#aaaaaa; line-height:1.6;">
              인증 코드 유효시간은 5분입니다.<br>
              본인이 요청하지 않았다면 이 메일을 무시해 주세요.
            </td>
          </tr>
          <tr>
            <td align="center" bgcolor="#fafafa" class="footer" style="padding:20px 32px; background-color:#fafafa; font-size:12px; color:#bbbbbb;">
              © 투리브 (Touribe). All rights reserved.
            </td>
          </tr>
        </table>
      </td></tr>
    </table>
    </body>
    </html>
    """.formatted(imageUrl, title, code, description);
	}

	// 공통 발송 로직
	private void sendHtmlMail(String toEmail, String subject, String html) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(toEmail);
			helper.setSubject(subject);
			helper.setText(html, true); // true = HTML로 렌더링

			mailSender.send(message);
		} catch (MessagingException e) {
			throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, e.getMessage());
		}
	}
}
