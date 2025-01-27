package org.example.be.oauth.repository;


import org.example.be.oauth.entity.SocialUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialUserRepository extends JpaRepository<SocialUser, Long> {

    Optional<SocialUser> findByUserIdentifier(String userIdentifier);
    Optional<SocialUser> findByProviderId(String providerId);

}
