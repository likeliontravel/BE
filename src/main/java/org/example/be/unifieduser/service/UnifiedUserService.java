package org.example.be.unifieduser.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be.generaluser.repository.GeneralUserRepository;
import org.example.be.oauth.repository.SocialUserRepository;
import org.example.be.unifieduser.dto.ModifyNameDTO;
import org.example.be.unifieduser.dto.PolicyUpdateRequestDTO;
import org.example.be.unifieduser.dto.SubscribedUpdateRequestDTO;
import org.example.be.unifieduser.dto.UnifiedUserCreationRequestDTO;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnifiedUserService {

    private final UnifiedUserRepository unifiedUserRepository;
    private final SocialUserRepository socialUserRepository;
    private final GeneralUserRepository generalUserRepository;

    // 통합 유저 생성 메서드 ( 컨트롤러 호출이 아닙니다. 소셜, 일반 각 서비스 생성메서드에서 호출합니다.
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
    @Transactional
    public void deleteUnifiedUser(String userIdentifier) {

        UnifiedUser unifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 통합 유저를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        // unified user 삭제. Cascade와 OrphanRemoval옵션으로 연결된 데이터(소셜 또는 일반) 자동삭제.
        unifiedUserRepository.delete(unifiedUser);
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
















}
