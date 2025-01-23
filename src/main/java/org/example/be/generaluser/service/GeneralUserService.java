package org.example.be.generaluser.service;


import lombok.RequiredArgsConstructor;
import org.example.be.generaluser.domain.GeneralUser;
import org.example.be.generaluser.dto.GeneralUserDTO;
import org.example.be.generaluser.repository.GeneralUserRepository;
import org.example.be.unifieduser.dto.UnifiedUserCreationRequestDTO;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

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
        generalUser.setPassword(passwordEncoder.encode(generalUserDTO.getPassword()));
        generalUser.setName(generalUserDTO.getName());
        generalUser.setRole("ROLE_USER");

        generalUserRepository.save(generalUser);

        // 통합 유저객체 생성 및 저장 메서드 호출
        unifiedUserService.createUnifiedUser(
                new UnifiedUserCreationRequestDTO("gen", generalUserDTO.getEmail(), generalUserDTO.getName(), "ROLE_USER")
        );

    }

    // 회원 수정 로직
    @Transactional
    public void updateGeneralUser(GeneralUserDTO generalUserDTO) {

        GeneralUser generalUser = generalUserRepository.findByUserIdentifier(generalUserDTO.getEmail())
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다"));

        // 유효하지 않은 요청 데이터인 경우
        if (generalUserDTO.getPassword() == null && generalUserDTO.getName() == null) {

            throw new IllegalArgumentException("요청 데이터에 유효한 정보가 없습니다. 이메일과 이름 중 하나 이상은 값을 입력해야 합니다.");
        }

        // 필요한 데이터만 업데이트
        if (generalUserDTO.getPassword() != null) {
            generalUser.setPassword(passwordEncoder.encode(generalUserDTO.getPassword()));
        }

        if (generalUserDTO.getName() != null) {
            generalUser.setName(generalUserDTO.getName());
//            unifiedUserService.updateName(generalUser.getUnifiedUser().getId(), generalUserDTO.getName());  // 통합테이블 구현 후 주석 해제. 만들 때 조회를 getId로 하는게 맞는지 재차확인할 것.
        }

        generalUserRepository.save(generalUser);
    }
    // 이메일을 기준으로 회원 프로필 정보 조회
    @Transactional(readOnly = true)
    public GeneralUserDTO getProfile(String email) {
        GeneralUser generalUser = generalUserRepository.findByEmail(email)
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
//    // 회원 탈퇴 로직
//    public void deleteGeneralUser(GeneralUserDTO generalUserDTO) {
//
//        Optional<GeneralUser> userOptional = generalUserRepository.findByEmail(generalUserDTO.getEmail());
//
//        if (userOptional.isPresent()) {
//
//            GeneralUser generalUser = userOptional.get();
//
////            unifiedUserService.deleteUnifiedUser(user.getUnifiedUser().getId());    // 통합 테이블 구현 후 주석 해제. 만들 때 조회를 getId로 하는게 맞는지 재차확인할 것.
//            generalUserRepository.delete(generalUser);
//
//        } else {
//
//            throw new NoSuchElementException("회원을 찾을 수 없습니다.");
//        }
//    }

