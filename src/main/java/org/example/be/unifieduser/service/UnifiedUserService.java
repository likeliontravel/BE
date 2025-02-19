package org.example.be.unifieduser.service;


import lombok.RequiredArgsConstructor;
import org.example.be.generaluser.repository.GeneralUserRepository;
import org.example.be.oauth.repository.SocialUserRepository;
import org.example.be.unifieduser.dto.PolicyUpdateRequestDTO;
import org.example.be.unifieduser.dto.SubscribedUpdateRequestDTO;
import org.example.be.unifieduser.dto.UnifiedUserCreationRequestDTO;
import org.example.be.unifieduser.dto.UnifiedUserProfileDTO;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnifiedUserService {

    private final UnifiedUserRepository unifiedUserRepository;
    private final SocialUserRepository socialUserRepository;
    private final GeneralUserRepository generalUserRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    // 공통 메서드: 통합 유저 조회
    @Transactional(readOnly = true)
    public UnifiedUser getUnifiedUser(String userIdentifier) {
        return unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));
    }

    // 통합 유저 생성 메서드 ( 컨트롤러 호출이 아닙니다. 소셜, 일반 각 서비스 생성메서드에서 호출합니다. )
    @Transactional
    public UnifiedUser createUnifiedUser(UnifiedUserCreationRequestDTO dto) {

        String provider = dto.getProvider();
        String email = dto.getEmail();
        String name = dto.getName();
        String role = dto.getRole();
        String userIdentifier = provider + " " + email;

        UnifiedUser unifiedUser = new UnifiedUser();
        unifiedUser.setUserIdentifier(userIdentifier);
        unifiedUser.setEmail(email);
        unifiedUser.setName(name);
        unifiedUser.setRole(role);
        unifiedUser.setPolicyAgreed(false);
        unifiedUser.setSubscribed(false);



        return unifiedUserRepository.save(unifiedUser);
    }

    // 통합 유저 삭제 ( 회원 탈퇴 )
/*    @Transactional
    public void deleteUnifiedUser(String userIdentifier) {

        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        // unified user 삭제. Cascade와 OrphanRemoval옵션으로 연결된 데이터(소셜 또는 일반) 자동삭제.
        unifiedUserRepository.delete(unifiedUser);
        unifiedUserRepository.flush();  // DB에 반영해줌


        if (userIdentifier.startsWith("gen ")) {
            generalUserRepository.findByUserIdentifier(userIdentifier)
                    .ifPresent(generalUserRepository::delete);
        } else {
            socialUserRepository.findByUserIdentifier(userIdentifier)
                    .ifPresent(socialUserRepository::delete);
        }
    }*/
    //리팩토링 유저 삭제
    @Transactional
    public void deleteUnifiedUser(String userIdentifier) {
        UnifiedUser unifiedUser = getUnifiedUser(userIdentifier);
        unifiedUserRepository.delete(unifiedUser);
        unifiedUserRepository.flush();

        if (userIdentifier.startsWith("gen ")) {
            generalUserRepository.findByUserIdentifier(userIdentifier).ifPresent(generalUserRepository::delete);
        } else {
            socialUserRepository.findByUserIdentifier(userIdentifier).ifPresent(socialUserRepository::delete);
        }
    }


    @Transactional
    // 약관 동의 상태 변경 - 원하는 상태는 프론트에서 보내줘야됩니다.
/*    public void updatePolicy(PolicyUpdateRequestDTO dto) {
        String userIdentifier = dto.getUserIdentifier();
        Boolean policyAgreed = dto.getPolicyAgreed();

        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        unifiedUser.setPolicyAgreed(policyAgreed);
        unifiedUserRepository.save(unifiedUser);
    }*/
    public void updatePolicy(PolicyUpdateRequestDTO dto) {
        UnifiedUser unifiedUser = getUnifiedUser(dto.getUserIdentifier());
        unifiedUser.setPolicyAgreed(dto.getPolicyAgreed());
        unifiedUserRepository.save(unifiedUser);
    }

    // 구독 여부 상태 변경 - 원하는 상태는 프론트에서 보내줘야됩니다.
    @Transactional
/*    public void updateSubscribed(SubscribedUpdateRequestDTO dto) {
        String userIdentifier = dto.getUserIdentifier();
        Boolean subscribed = dto.getSubscribed();

        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        unifiedUser.setSubscribed(subscribed);
        unifiedUserRepository.save(unifiedUser);
    }*/
    public void updateSubscribed(SubscribedUpdateRequestDTO dto) {
        UnifiedUser unifiedUser = getUnifiedUser(dto.getUserIdentifier());
        unifiedUser.setSubscribed(dto.getSubscribed());
        unifiedUserRepository.save(unifiedUser);
    }

    // 유저 프로필 조회
    @Transactional(readOnly = true)
    public UnifiedUserProfileDTO getUserProfile(String userIdentifier) {
        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        UnifiedUserProfileDTO profileDTO = new UnifiedUserProfileDTO();
        profileDTO.setEmail(unifiedUser.getEmail());
        profileDTO.setName(unifiedUser.getName());
        profileDTO.setSubscribed(unifiedUser.getSubscribed());  // 추가됨
        profileDTO.setAdditionalInfo(unifiedUser.getSocialProvider() != null ? "소셜 유저" : "일반 유저");
        profileDTO.setProvider(unifiedUser.getSocialProvider());

        return profileDTO;
    }

    // 프로필 사진 수정
    @Transactional
/*    public void updateProfilePicture(String userIdentifier, String newProfilePictureUrl) {
        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        unifiedUser.setProfilePictureUrl(newProfilePictureUrl); // 프로필 사진 URL 업데이트
        unifiedUserRepository.save(unifiedUser);
    }*/
    public void updateProfilePicture(String userIdentifier, String newProfilePictureUrl) {
        UnifiedUser unifiedUser = getUnifiedUser(userIdentifier);
        unifiedUser.setProfilePictureUrl(newProfilePictureUrl);
        unifiedUserRepository.save(unifiedUser);
    }

    // 이름 수정 은 없어되잖아?
    @Transactional
    public void updateName(String userIdentifier, String newName) {
        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        unifiedUser.setName(newName);
        unifiedUserRepository.save(unifiedUser);
    }

    // 이메일 수정 (일반 유저만)
    @Transactional
/*    public void updateEmail(String userIdentifier, String newEmail) {
        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        // 일반 유저가 아닌 경우 이메일 변경 제한
        if (unifiedUser.getSocialProvider() != null) {
            throw new IllegalStateException("소셜 로그인 사용자는 이메일을 변경할 수 없습니다.");
        }

        unifiedUser.setEmail(newEmail);
        unifiedUserRepository.save(unifiedUser);
    }*/
    public void updateEmail(String userIdentifier, String newEmail) {
        UnifiedUser unifiedUser = getUnifiedUser(userIdentifier);
        if (unifiedUser.getSocialProvider() != null) {
            throw new IllegalStateException("소셜 로그인 사용자는 이메일을 변경할 수 없습니다.");
        }
        unifiedUser.setEmail(newEmail);
        unifiedUserRepository.save(unifiedUser);
    }

    // 비밀번호 수정 (일반 로그인 사용자)
    @Transactional
/*    public void updatePassword(String userIdentifier, String newPassword) {
        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(newPassword);
        unifiedUser.setPassword(encodedPassword);  // 비밀번호 업데이트 (암호화된 형태로 저장해야 함)
        unifiedUserRepository.save(unifiedUser);

        if (unifiedUser.getSocialProvider() != null) {
            throw new IllegalStateException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }
    }*/
    public void updatePassword(String userIdentifier, String newPassword) {
        UnifiedUser unifiedUser = getUnifiedUser(userIdentifier);
        if (unifiedUser.getSocialProvider() != null) {
            throw new IllegalStateException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }
        unifiedUser.setPassword(passwordEncoder.encode(newPassword));
        unifiedUserRepository.save(unifiedUser);
    }

    // 소셜 계정 연동
    @Transactional
/*    public void linkSocialAccount(String userIdentifier, String socialProvider, String socialIdentifier) {
        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        // 소셜 계정 정보를 저장하는 로직 (소셜 로그인 연동을 위한 처리)
        unifiedUser.setSocialProvider(socialProvider);
        unifiedUser.setSocialIdentifier(socialIdentifier);
        unifiedUserRepository.save(unifiedUser);

    }*/
    public void linkSocialAccount(String userIdentifier, String socialProvider, String socialIdentifier) {
        UnifiedUser unifiedUser = getUnifiedUser(userIdentifier);
        unifiedUser.setSocialProvider(socialProvider);
        unifiedUser.setSocialIdentifier(socialIdentifier);
        unifiedUserRepository.save(unifiedUser);
    }

    // 소셜 유저 여부 확인
    public boolean isSocialUser(String userIdentifier) {
        return getUnifiedUser(userIdentifier).getSocialProvider() != null;
    }
}
