package org.example.be.generaluser.service;

//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be.generaluser.domain.GeneralUser;
import org.example.be.generaluser.dto.GeneralUserDTO;
import org.example.be.generaluser.dto.GeneralUserUpdatePasswordDTO;
import org.example.be.generaluser.repository.GeneralUserRepository;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.dto.UnifiedUserCreationRequestDTO;

import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeneralUserService {

    // 의존성 주입
    private final GeneralUserRepository generalUserRepository;
    //사용자 비밀번호 암호화 하기 위한 수단
    private final PasswordEncoder passwordEncoder;

    private final UnifiedUserService unifiedUserService;

    // 회원 가입 로직
    @Transactional
    public void signUp(GeneralUserDTO generalUserDTO) {

        GeneralUser generalUser = new GeneralUser();

        generalUser.setEmail(generalUserDTO.getEmail());
        if (generalUserDTO.getPassword() != null){
            generalUser.setPassword(passwordEncoder.encode(generalUserDTO.getPassword()));
        } else {
            throw new IllegalArgumentException("비밀번호가 비어있습니다.");
        }
        generalUser.setName(generalUserDTO.getName());
        generalUser.setRole("ROLE_USER");

        generalUserRepository.save(generalUser);

        // 통합 유저객체 생성 및 저장 메서드 호출
        unifiedUserService.createUnifiedUser(
                new UnifiedUserCreationRequestDTO("gen", generalUserDTO.getEmail(), generalUserDTO.getName(), "ROLE_USER")
        );

    }

    // 비밀번호 변경 로직
    @Transactional
    public void updatePassword(GeneralUserUpdatePasswordDTO dto) {
        String inputUserIdentifier = "gen_" + dto.getEmail();
        if (!inputUserIdentifier.equals(SecurityUtil.getUserIdentifierFromAuthentication())) {
            throw new IllegalArgumentException("인증 정보가 일치하지 않거나 비밀번호를 변경할 권한이 없습니다.");
        }

        GeneralUser generalUser = generalUserRepository.findByUserIdentifier(inputUserIdentifier)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다. userIdentifier: " + inputUserIdentifier));

        if (dto.getPassword() == null) {
            throw new IllegalArgumentException("비밀번호를 입력해야 합니다.");
        }

        generalUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        generalUserRepository.save(generalUser);
    }

    // 이메일을 기준으로 회원 프로필 정보 조회
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