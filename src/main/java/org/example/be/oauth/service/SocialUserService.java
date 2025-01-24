package org.example.be.oauth.service;

import org.example.be.oauth.dto.SocialUserDTO;
import org.example.be.oauth.entity.SocialUser;
import org.example.be.oauth.repository.SocialUserRepository;
import org.springframework.stereotype.Service;

@Service
public class SocialUserService {

    private final SocialUserRepository socialUserRepository;

    public SocialUserService(SocialUserRepository socialUserRepository) {
        this.socialUserRepository = socialUserRepository;
    }

    public SocialUserDTO getUserProfile(String userIdentifier) {
        SocialUser socialUser = socialUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userIdentifier : " + userIdentifier));

        return convertEntityToDTO(socialUser); // 변환 메서드 호출
    }

    // Entity -> DTO 변환 메서드
    public SocialUserDTO convertEntityToDTO(SocialUser entity) {
        SocialUserDTO dto = new SocialUserDTO();
        dto.setUserIdentifier(entity.getUserIdentifier());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());  // 이메일도 설정
        dto.setRole(entity.getRole());
        return dto;
    }

    // DTO -> Entity 변환 메서드 //확장성을 고려해서 코드 수정 (유지보수 코드값)
    public SocialUser convertDTOToEntity(SocialUserDTO dto) {
        SocialUser entity = new SocialUser();
        entity.setUserIdentifier(dto.getUserIdentifier());
        entity.setName(dto.getName());
        entity.setRole(dto.getRole());
        entity.setEmail(dto.getEmail());  // 이메일도 설정
        return entity;
    }
}