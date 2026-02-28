package org.example.be.generaluser.service;

import java.util.NoSuchElementException;

import org.example.be.exception.custom.InvalidInvitationException;
import org.example.be.generaluser.domain.GeneralUser;
import org.example.be.generaluser.dto.GeneralUserDTO;
import org.example.be.generaluser.repository.GeneralUserRepository;
import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.member.entity.Member;
import org.example.be.member.repository.MemberRepository;
import org.example.be.unifieduser.dto.UnifiedUserCreationRequestDTO;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeneralUserService {

	// 의존성 주입
	private final GeneralUserRepository generalUserRepository;
	//사용자 비밀번호 암호화 하기 위한 수단
	private final PasswordEncoder passwordEncoder;

	private final UnifiedUserService unifiedUserService;
	private final MemberRepository memberRepository;

	private final GroupInvitationService groupInvitationService;
	private final GroupService groupService;

	// 회원 가입 로직
	@Transactional
	public void signUp(GeneralUserDTO generalUserDTO) {

		GeneralUser generalUser = new GeneralUser();

		generalUser.setEmail(generalUserDTO.getEmail());

		if (generalUserDTO.getPassword() == null || generalUserDTO.getPassword().isBlank()) {
			throw new IllegalArgumentException("비밀번호가 비어있습니다.");
		}

		generalUser.setPassword(passwordEncoder.encode(generalUserDTO.getPassword()));
		generalUser.setName(generalUserDTO.getName());
		generalUser.setRole("ROLE_USER");

		generalUserRepository.save(generalUser);

		// 통합 유저객체 생성 및 저장 메서드 호출
		unifiedUserService.createUnifiedUser(
			new UnifiedUserCreationRequestDTO("gen", generalUserDTO.getEmail(), generalUserDTO.getName(), "ROLE_USER")
		);

	}

	// 초대 코드가 있을 경우 자동 그룹 가입 처리 포함 회원 가입
	@Transactional
	public void signUpWithInvitation(GeneralUserDTO generalUserDTO, String invitationCode) {
		// 일반회원가입 로직
		signUp(generalUserDTO);

		// 초대 코드가 있을 경우 그룹 자동 가입
		if (invitationCode != null && !invitationCode.isBlank()) {
			GroupInvitation invitation = groupInvitationService.getValidInvitation(invitationCode);
			if (invitation == null || invitation.getGroup() == null) {
				throw new InvalidInvitationException("유효하지 않은 초대 코드입니다.");
			}

			try {
				Member user = memberRepository.findByEmail(generalUserDTO.getEmail())
					.orElseThrow(() -> new NoSuchElementException("Member 정보 없음"));
				groupService.addMemberToGroup(invitation.getGroup().getGroupName(), user.getId());
			} catch (Exception e) {
				// TODO: GeneralUser 회원가입이 Member시스템에서 완성되면 제거
				System.out.println("회원 가입 후 그룹 자동 가입 실패 (Member 시스템 미연동): " + e.getMessage());
			}
		}
	}

	// 이메일을 기준으로 회원 프로필 정보 조회
	// 실서비스 미사용
	@Transactional(readOnly = true)
	public GeneralUserDTO getProfile(String email) {
		GeneralUser generalUser = generalUserRepository.findByUserIdentifier("gen_" + email)
			.orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));

		// GeneralUser를 DTO로 변환 후 반환
		GeneralUserDTO generalUserDTO = new GeneralUserDTO();
		generalUserDTO.setId(generalUser.getId());
		generalUserDTO.setEmail(generalUser.getEmail());
		generalUserDTO.setName(generalUser.getName());
		generalUserDTO.setRole(generalUser.getRole());
		generalUserDTO.setUserIdentifier(generalUser.getUserIdentifier());

		return generalUserDTO;
	}
}