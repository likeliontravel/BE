package org.example.be.oauth.repository;


import org.example.be.oauth.entity.SocialUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialUserRepository extends JpaRepository<SocialUserEntity, Long> {

    Optional<SocialUserEntity> findByEmail(String email);

    Optional<SocialUserEntity> findByProviderAndProviderId(String provider, String providerId);

    SocialUserEntity findByUsername(String username);
}
