package org.example.be.member.controller;

import org.example.be.member.dto.MemberDto;
import org.example.be.member.dto.MemberJoinReqBody;
import org.example.be.member.entity.Member;
import org.example.be.member.service.MemberService;
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
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@PostMapping
	public ResponseEntity<CommonResponse<MemberDto>> join(@Valid @RequestBody MemberJoinReqBody reqBody) {

		Member member = memberService.join(reqBody);
		MemberDto memberDto = MemberDto.from(member);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(memberDto, "회원가입 성공"));
	}

}
