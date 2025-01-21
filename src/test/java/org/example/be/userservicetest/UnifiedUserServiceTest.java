package org.example.be.userservicetest;

import org.example.be.oauth.entity.SocialUser;
import org.example.be.oauth.repository.SocialUserRepository;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class UnifiedUserServiceTest {

    @Autowired
    private UnifiedUserService unifiedUserService;

    @Autowired
    private UnifiedUserRepository unifiedUserRepository;

    @Autowired
    private SocialUserRepository socialUserRepository;

    private UnifiedUser unifiedUser;
    private SocialUser socialUser;

    @BeforeEach
    void setUp() {
        socialUser = new SocialUser();
        socialUser.setEmail("testuser@exam.com");
        socialUser.setName("Test User");
        socialUser.setProvider("google");
        socialUser.setProviderId("google-id-123");
        socialUser.setRole("ROLE_USER");
        socialUser.setUserIdentifier("google testuser@exam.com");
        socialUserRepository.save(socialUser);

        unifiedUser = new UnifiedUser();
        unifiedUser.setUserIdentifier(socialUser.getUserIdentifier());
        unifiedUser.setEmail(socialUser.getEmail());
        unifiedUser.setName(socialUser.getName());
        unifiedUser.setRole(socialUser.getRole());
        unifiedUser.setPolicyAgreed(true);
        unifiedUser.setSubscribed(false);
        unifiedUserRepository.save(unifiedUser);

    }

    @Test
    void testDeleteSocialUser() {
        Optional<UnifiedUser> beforeUnifiedUser = unifiedUserRepository.findByUserIdentifier(unifiedUser.getUserIdentifier());
        Optional<SocialUser> beforeSocialUser = socialUserRepository.findByUserIdentifier(socialUser.getUserIdentifier());

        unifiedUserService.deleteUnifiedUser(unifiedUser.getUserIdentifier());

        Optional<UnifiedUser> afterUnifiedUser = unifiedUserRepository.findByUserIdentifier(unifiedUser.getUserIdentifier());
        Optional<SocialUser> afterSocialUser = socialUserRepository.findByUserIdentifier(socialUser.getUserIdentifier());

        Assertions.assertFalse(afterUnifiedUser.isPresent(), "unifieduser should be deleted");
        Assertions.assertFalse(afterSocialUser.isPresent(), "socialUserShould be deleted");

    }



}
