package org.example.be.unifieduser.service;

import org.example.be.gcs.GCSService;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.dto.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be.generaluser.repository.GeneralUserRepository;
import org.example.be.oauth.repository.SocialUserRepository;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnifiedUserService {

    private final UnifiedUserRepository unifiedUserRepository;
    private final SocialUserRepository socialUserRepository;
    private final GeneralUserRepository generalUserRepository;
    private final GCSService gcsService;

    // 통합 유저 생성 메서드 ( 컨트롤러 호출이 아닙니다. 소셜, 일반 각 서비스 생성메서드에서 호출합니다. )
    @Transactional
    public UnifiedUser createUnifiedUser(UnifiedUserCreationRequestDTO dto) {
        String userIdentifier = dto.getProvider() + "_" + dto.getEmail();

        Optional<UnifiedUser> existingUser = unifiedUserRepository.findByUserIdentifier(userIdentifier);

        if (existingUser.isPresent()) {
            return existingUser.get(); // 이미 존재하면 기존 사용자 반환
        }

        UnifiedUser unifiedUser = new UnifiedUser();
        unifiedUser.setUserIdentifier(userIdentifier);
        unifiedUser.setEmail(dto.getEmail());
        unifiedUser.setName(dto.getName()); // 최초 로그인 시에만 `name` 저장
        unifiedUser.setRole(dto.getRole());
        unifiedUser.setPolicyAgreed(false);
        unifiedUser.setSubscribed(false);

        return unifiedUserRepository.save(unifiedUser);
    }

    // 통합 유저 삭제 ( 회원 탈퇴 )
    @Transactional
    public void deleteUnifiedUser(String userIdentifier) {

        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        // unified user 삭제. Cascade와 OrphanRemoval옵션으로 연결된 데이터(소셜 또는 일반) 자동삭제.
        unifiedUserRepository.delete(unifiedUser);
        unifiedUserRepository.flush();  // DB에 반영해줌
//
        if (userIdentifier.startsWith("gen_")) {
            generalUserRepository.findByUserIdentifier(userIdentifier)
                    .ifPresent(generalUserRepository::delete);
        } else {
            socialUserRepository.findByUserIdentifier(userIdentifier)
                    .ifPresent(socialUserRepository::delete);
        }
    }

    @Transactional
    // 약관 동의 상태 변경 - 원하는 상태는 프론트에서 보내줘야됩니다.
    public void updatePolicy(PolicyUpdateRequestDTO dto) {
        String userIdentifier = dto.getUserIdentifier();
        Boolean policyAgreed = dto.getPolicyAgreed();

        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        unifiedUser.setPolicyAgreed(policyAgreed);
        unifiedUserRepository.save(unifiedUser);
    }

    // 구독 여부 상태 변경 - 원하는 상태는 프론트에서 보내줘야됩니다.
    @Transactional
    public void updateSubscribed(SubscribedUpdateRequestDTO dto) {
        String userIdentifier = dto.getUserIdentifier();
        Boolean subscribed = dto.getSubscribed();

        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        unifiedUser.setSubscribed(subscribed);
        unifiedUserRepository.save(unifiedUser);
    }

    // 회원정보 변경
    // 가볍게 이름만! : 형진이형 마이페이지 합칠 때 참고하려고요
    @Transactional
    public void updateName(ModifyNameDTO dto) {
        String userIdentifier = dto.getUserIdentifier();
        String name = dto.getName();

        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        unifiedUser.setName(name);
        unifiedUserRepository.save(unifiedUser);
    }

    // 해당 userIdentifier를 가진 유저가 존재하는지 확인하는 메서드
    public boolean unifiedUserExists(String userIdentifier) {
        return unifiedUserRepository.findByUserIdentifier(userIdentifier).isPresent();
    }

    // 유저 정보 조회하기
    @Transactional(readOnly = true)
    public UnifiedUserDTO getUserProfile(String userIdentifier) {
        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("이 userIdentifier의 유저를 찾을 수 없습니다. userIdentifier: " + userIdentifier));

        UnifiedUserDTO unifiedUserDTO = new UnifiedUserDTO();
        unifiedUserDTO.setId(unifiedUser.getId());
        unifiedUserDTO.setName(unifiedUser.getName());
        unifiedUserDTO.setEmail(unifiedUser.getEmail());
        unifiedUserDTO.setRole(unifiedUser.getRole());
        unifiedUserDTO.setPolicyAgreed(unifiedUser.getPolicyAgreed());
        unifiedUserDTO.setSubscribed(unifiedUser.getSubscribed());
        unifiedUserDTO.setProfileImageUrl(unifiedUser.getProfileImageUrl());

        return unifiedUserDTO;
    }

    // 프로필 사진 업데이트
    @Transactional
    public String updateProfileImageUrl(String userIdentifier, MultipartFile file) throws IOException {
        // 요청자 본인의 프로필 사진 변경 시도인지 확인
        if (!userIdentifier.equals(SecurityUtil.getUserIdentifierFromAuthentication())) {
            throw new IllegalArgumentException("본인의 프로필 사진만 변경할 수 있습니다.");
        }

        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("이 userIdentifier의 유저를 찾을 수 없습니다. userIdentifier: " + userIdentifier));

        // 이미 프로필 사진이 있다면 삭제하고 저장
        if (user.getProfileImageUrl() != null) {
            gcsService.deleteProfileImage(user.getProfileImageUrl());
        }

        String profileImageUrl = gcsService.uploadProfileImage(file, userIdentifier);
        user.setProfileImageUrl(profileImageUrl);
        return profileImageUrl;
    }

    // 프로필 사진 삭제
    @Transactional
    public void deleteProfileImage(String userIdentifier) {
        // 요청자 본인의 프로필 사진 삭제 요청 시도인지 확인
        if (!userIdentifier.equals(SecurityUtil.getUserIdentifierFromAuthentication())) {
            throw new IllegalArgumentException("본인의 프로필 사진만 삭제할 수 있습니다.");
        }

        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("이 userIdentifier의 유저를 찾을 수 없습니다. userIdentifier: " + userIdentifier));

        if (user.getProfileImageUrl() != null) {
            gcsService.deleteProfileImage(user.getProfileImageUrl());
            user.setProfileImageUrl(null);
        }

    }

    // userIdentifier로 이름 가져오기
    public String getNameByUserIdentifier(String userIdentifier) {
        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("이 userIdentifier의 유저를 찾을 수 없습니다. userIdentifier: " + userIdentifier));

        return user.getName();
    }

    // userIdentifier로 이름, 프로필 사진만 가져오기 ( 그룹 채팅에서만 이용 용도, 가독성 및 안정성 상 불필요시 삭제 예정 )
    public UnifiedUsersNameAndProfileImageUrl getNameAndProfileImageUrlByUserIdentifier(String userIdentifier) {
        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("이 userIdentifier의 유저를 찾을 수 없습니다. userIdentifier: " + userIdentifier));

        UnifiedUsersNameAndProfileImageUrl dto = new UnifiedUsersNameAndProfileImageUrl();
        dto.setName(user.getName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        return dto;
    }
}

// check!
// 개념 상
// 프로필 사진 변경 / 업데이트 메서드에서 마지막에 JpaRepository.save() 호출 없이도 db에 잘 반영됨.
// @Transactional 로 인해서 영속성을 가진 엔티티 내부 필드값 변경이 있어도 트랜잭션 커밋 시점에 자동으로 DB에 적용되도록 되어 있음.
// 확인해보자.