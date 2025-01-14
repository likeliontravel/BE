package org.example.be.oauth.repository;


import org.example.be.oauth.entity.SocialUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSocialRepository extends JpaRepository<SocialUserEntity, Long> {

    // 기존 username 기반 조회
    SocialUserEntity findByUsername(String username);

    // 추가: 사용자 이름(name) 기반 조회
    SocialUserEntity findByName(String name);
}