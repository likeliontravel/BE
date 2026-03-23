package org.example.be.member.service;

import java.util.Optional;

import org.example.be.member.dto.MemberJoinReqBody;
import org.example.be.member.dto.PasswordUpdateReqBody;
import org.example.be.member.entity.Member;
import org.example.be.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public Member join(MemberJoinReqBody reqBody) {
		if (memberRepository.existsByEmail(reqBody.email())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다.");
		}
		String password = passwordEncoder.encode(reqBody.password());
		Member member = Member.createForJoin(reqBody.email(), reqBody.name(), password);
		return memberRepository.save(member);

	}

	public Member authenticateAndGetMember(String email, String password) {
		Member member = findByEmail(email)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
		checkPassword(member, password);
		return member;
	}

	public Optional<Member> findByEmail(String email) {
		return memberRepository.findByEmail(email);
	}

	public void checkPassword(Member member, String password) {
		if (!passwordEncoder.matches(password, member.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다.");
		}
	}

	public Member getById(long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));
	}

	// 비밀번호 변경 로직
	@Transactional
	public void updatePassword(Long memberId, PasswordUpdateReqBody reqBody) {

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

		String newEncodedPassword = passwordEncoder.encode(reqBody.password());
		member.changePassword(newEncodedPassword);

		memberRepository.save(member);
	}

	public void updateName(long memberId, String name) {

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

		member.changeName(name);

		memberRepository.save(member);
	}
}
