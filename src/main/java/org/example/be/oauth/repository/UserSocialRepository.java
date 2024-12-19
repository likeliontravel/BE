package org.example.be.oauth.repository;


import org.example.be.oauth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSocialRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByUsername(String username);
}
