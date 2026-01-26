package org.example.be.member.service;

import org.example.be.member.dto.MemberJoinReqBody;
import org.example.be.member.entity.Member;
import org.example.be.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;

	public Member join(MemberJoinReqBody reqBody) {
		if (memberRepository.existsByEmail(reqBody.email())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다.");
		}
	}
}
