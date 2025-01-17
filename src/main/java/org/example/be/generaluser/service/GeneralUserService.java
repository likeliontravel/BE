package org.example.be.generaluser.service;

import lombok.RequiredArgsConstructor;
import org.example.be.generaluser.domain.GeneralUser;
import org.example.be.generaluser.dto.GeneralUserDTO;
import org.example.be.generaluser.repository.GeneralUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeneralUserService {

    // 의존성 주입
    private final GeneralUserRepository generalUserRepository;
    //사용자 비밀번호 암호화 하기 위한 수단
    private final PasswordEncoder passwordEncoder;
//    private final UnifiedUserService unifiedUserService;  // 잠시 의존성 주석처리. 통합 테이블 구현 후 주석 해제

    // 회원 가입 로직
    public void signUp(GeneralUserDTO generalUserDTO) {

        // 통합 유저객체 생성. 통합테이블 구현 후 주석 해제
//        UnifiedUser unifiedUser = unifiedUserService.createUnifiedUser(
//                "gen", generalUserDTO.getEmail(), generalUserDTO.getName(), "ROLE_USER", false, false
//        );

        GeneralUser generalUser = new GeneralUser();

        generalUser.setEmail(generalUserDTO.getEmail());
        generalUser.setPassword(passwordEncoder.encode(generalUserDTO.getPassword()));
        generalUser.setName(generalUserDTO.getName());
        generalUser.setRole("ROLE_USER");
//        generalUser.setUnifiedUser(unifiedUser);    // 통합 테이블 구현 후 주석 해제

        generalUserRepository.save(generalUser);
    }

    // 회원 수정 로직
    public void updateGeneralUser(GeneralUserDTO generalUserDTO) {

        GeneralUser generalUser = generalUserRepository.findByEmail(generalUserDTO.getEmail())
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

    // 회원 탈퇴 로직
    public void deleteGeneralUser(GeneralUserDTO generalUserDTO) {

        Optional<GeneralUser> userOptional = generalUserRepository.findByEmail(generalUserDTO.getEmail());

        if (userOptional.isPresent()) {

            GeneralUser generalUser = userOptional.get();

//            unifiedUserService.deleteUnifiedUser(user.getUnifiedUser().getId());    // 통합 테이블 구현 후 주석 해제. 만들 때 조회를 getId로 하는게 맞는지 재차확인할 것.
            generalUserRepository.delete(generalUser);

        } else {

            throw new NoSuchElementException("회원을 찾을 수 없습니다.");
        }
    }

//    // 구독 동의 로직 -> 임시 삭제처리. 구독관련은 통합서비스쪽에서 구현 후 이 클래스에서는 확실 삭제 예정
//    public void subscribeAgree(GeneralUserDTO generalUserDTO) {
//
//        Optional<GeneralUser> userOptional = generalUserRepository.findByEmail(generalUserDTO.getEmail());
//
//        if (userOptional.isPresent()) {
//
//            GeneralUser generalUser = userOptional.get();
//
//            generalUser.setUserSubscribe(generalUserDTO.getSubscribe());
//
//            generalUserRepository.save(generalUser);
//
//        } else {
//
//            throw new NoSuchElementException("회원을 찾을 수 없습니다.");
//        }
//    }
}
