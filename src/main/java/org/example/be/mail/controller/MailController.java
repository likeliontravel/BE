package org.example.be.mail.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.mail.dto.MailDTO;
import org.example.be.mail.service.MailService;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailController {

    private final MailService mailService;

    // 메일 인증 코드 발급 받기
    @PostMapping("/send")
    public ResponseEntity<CommonResponse<String>> mailSend(@RequestBody MailDTO mailDTO) {

        mailService.sendMail(mailDTO.getEmail());

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "이메일 코드 받기 성공 : " + mailDTO.getEmail()));
    }

    // 메일 코드 인증
    @PostMapping("/verify")
    public ResponseEntity<CommonResponse<String>> mailVerify(@RequestBody MailDTO mailDTO) {

        try {

            boolean isVerified = mailService.verifyCode(mailDTO);

            if (isVerified) {

                return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(mailDTO.getEmail(),"이메일 인증 성공"));
            } else {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.error(400,"이메일 인증 실패"));
            }
        } catch (RuntimeException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.error(400,e.getMessage()));
        }
    }
}
