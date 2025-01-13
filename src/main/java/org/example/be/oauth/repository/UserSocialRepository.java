package org.example.be.oauth.repository;


import org.example.be.oauth.entity.SocialUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSocialRepository extends JpaRepository<SocialUserEntity, Long> {

    SocialUserEntity findByUsername(String username);
}