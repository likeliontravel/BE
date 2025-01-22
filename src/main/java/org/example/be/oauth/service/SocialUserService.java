package org.example.be.oauth.service;

import org.example.be.oauth.dto.SocialUserDTO;
import org.example.be.oauth.entity.SocialUserEntity;
import org.example.be.oauth.repository.UserSocialRepository;
import org.springframework.stereotype.Service;

@Service
public class SocialUserService {

    private final UserSocialRepository userSocialRepository;

    public SocialUserService(UserSocialRepository userSocialRepository) {
        this.userSocialRepository = userSocialRepository;
    }

    public SocialUserDTO getUserProfile(String name) {
        SocialUserEntity userEntity = userSocialRepository.findByName(name);
        if (userEntity == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        return convertEntityToDTO(userEntity); // 변환 메서드 호출
    }

    // Entity -> DTO 변환 메서드
    public SocialUserDTO convertEntityToDTO(SocialUserEntity entity) {
        SocialUserDTO dto = new SocialUserDTO();
        dto.setUsername(entity.getUsername());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());  // 이메일도 설정
        dto.setRole(entity.getRole());
        return dto;
    }

    // DTO -> Entity 변환 메서드 //확장성을 고려해서 코드 수정 (유지보수 코드값)
    public SocialUserEntity convertDTOToEntity(SocialUserDTO dto) {
        SocialUserEntity entity = new SocialUserEntity();
        entity.setUsername(dto.getUsername());
        entity.setName(dto.getName());
        entity.setRole(dto.getRole());
        entity.setEmail(dto.getEmail());  // 이메일도 설정
        return entity;
    }
}