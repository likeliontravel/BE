package org.example.be.domain.member.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.example.be.domain.member.dto.request.MemberJoinReqBody;
import org.example.be.domain.member.dto.request.PasswordUpdateReqBody;
import org.example.be.domain.member.dto.response.MemberProfileResBody;
import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.repository.MemberRepository;
import org.example.be.storage.gcs.GCSService;
import org.example.be.domain.member.type.OauthProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final GCSService gcsService;

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

	// 비밀번호 초기화 로직
	@Transactional
	public void resetPassword(String email, String newPassword) {
		Member member = findByEmail(email)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

		String newEncodedPassword = passwordEncoder.encode(newPassword);
		member.changePassword(newEncodedPassword);

		memberRepository.save(member);
	}

	// 비밀번호 변경 로직
	@Transactional
	public void updatePassword(Long memberId, PasswordUpdateReqBody reqBody) {
		Member member = getById(memberId);

		checkPassword(member, reqBody.oldPassword());

		String newEncodedPassword = passwordEncoder.encode(reqBody.newPassword());
		member.changePassword(newEncodedPassword);

		memberRepository.save(member);
	}

	@Transactional
	public void updateName(long memberId, String name) {
		Member member = getById(memberId);

		member.changeName(name);

		memberRepository.save(member);
	}

	@Transactional
	public String updateProfileImageUrl(Long memberId, MultipartFile file) throws IOException {
		Member member = getById(memberId);

		if (member.getProfileImageUrl() != null) {
			gcsService.deleteProfileImage(member.getProfileImageUrl());
		}

		String profileImageUrl = gcsService.uploadProfileImage(file, memberId);
		member.updateProfileImageUrl(profileImageUrl);

		return profileImageUrl;
	}

	@Transactional
	public void deleteProfileImage(long memberId) {
		Member member = getById(memberId);

		if (member.getProfileImageUrl() != null) {
			gcsService.deleteProfileImage(member.getProfileImageUrl());
			member.updateProfileImageUrl(null);
		}
	}

	@Transactional
	public void updatePolicyAgreed(long memberId, boolean policyAgreed) {
		Member member = getById(memberId);
		member.updatePolicyAgreed(policyAgreed);
	}

	@Transactional
	public void updateSubscribed(long memberId, boolean subscribed) {
		Member member = getById(memberId);
		member.updateSubscribed(subscribed);
	}

	public void deleteMember(long memberId) {
		Member member = getById(memberId);

		if (member.getProfileImageUrl() != null) {
			gcsService.deleteProfileImage(member.getProfileImageUrl());
		}

		memberRepository.delete(member);
	}

	public MemberProfileResBody getMemberProfile(Long memberId) {
		Member member = getById(memberId);
		return MemberProfileResBody.from(member);
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

	public boolean isPasswordExpired(Member member) {
		if (member.getOauthProvider() != OauthProvider.General) {
			return false; // 일반 회원이 아닌 경우(소셜 로그인) 비밀번호 변경 권장 대상 아님
		}

		return member.getPasswordChangedAt().isBefore(LocalDateTime.now().minusDays(30));
	}

}
