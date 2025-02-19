//package org.example.be.userservicetest;
//
//import org.example.be.oauth.entity.SocialUser;
//import org.example.be.oauth.repository.SocialUserRepository;
//<<<<<<< HEAD
//import org.example.be.unifieduser.dto.MyPageProfileDTO;
//=======
//>>>>>>> 예찬
//import org.example.be.unifieduser.entity.UnifiedUser;
//import org.example.be.unifieduser.repository.UnifiedUserRepository;
//import org.example.be.unifieduser.service.UnifiedUserService;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.Optional;
//
//@SpringBootTest
//public class UnifiedUserServiceTest {
//
//    @Autowired
//    private UnifiedUserService unifiedUserService;
//
//    @Autowired
//    private UnifiedUserRepository unifiedUserRepository;
//
//    @Autowired
//    private SocialUserRepository socialUserRepository;
//
//    private UnifiedUser unifiedUser;
//    private SocialUser socialUser;
//
//    @BeforeEach
//    void setUp() {
//        socialUser = new SocialUser();
//        socialUser.setEmail("testuser@exam.com");
//<<<<<<< HEAD
//        socialUser.setName("홍길동");
//=======
//        socialUser.setName("Test User");
//>>>>>>> 예찬
//        socialUser.setProvider("google");
//        socialUser.setProviderId("google-id-123");
//        socialUser.setRole("ROLE_USER");
//        socialUser.setUserIdentifier("google testuser@exam.com");
//        socialUserRepository.save(socialUser);
//
//        unifiedUser = new UnifiedUser();
//        unifiedUser.setUserIdentifier(socialUser.getUserIdentifier());
//        unifiedUser.setEmail(socialUser.getEmail());
//        unifiedUser.setName(socialUser.getName());
//        unifiedUser.setRole(socialUser.getRole());
//        unifiedUser.setPolicyAgreed(true);
//        unifiedUser.setSubscribed(false);
//        unifiedUserRepository.save(unifiedUser);
//
//    }
//
//    @Test
//    void testDeleteSocialUser() {
//        Optional<UnifiedUser> beforeUnifiedUser = unifiedUserRepository.findByUserIdentifier(unifiedUser.getUserIdentifier());
//        Optional<SocialUser> beforeSocialUser = socialUserRepository.findByUserIdentifier(socialUser.getUserIdentifier());
//
//        unifiedUserService.deleteUnifiedUser(unifiedUser.getUserIdentifier());
//
//        Optional<UnifiedUser> afterUnifiedUser = unifiedUserRepository.findByUserIdentifier(unifiedUser.getUserIdentifier());
//        Optional<SocialUser> afterSocialUser = socialUserRepository.findByUserIdentifier(socialUser.getUserIdentifier());
//
//<<<<<<< HEAD
//        Assertions.assertFalse(afterUnifiedUser.isPresent(), "회원 정보가 삭제되어야 합니다.");
//        Assertions.assertFalse(afterSocialUser.isPresent(), "소셜 회원 정보가 삭제되어야 합니다.");
//
//    }
////    @Test
////    void testGetUserProfileByEmail() {
////        MyPageProfileDTO profileDTO = unifiedUserService.getUserProfileByEmail("testuser@exam.com");
////
////        Assertions.assertNotNull(profileDTO, "프로필 정보는 null이 아니어야 합니다.");
////        Assertions.assertEquals("testuser@exam.com", profileDTO.getEmail(), "이메일이 일치해야 합니다.");
////        Assertions.assertEquals("홍길동", profileDTO.getName(), "이름이 일치해야 합니다.");
////        Assertions.assertEquals("ROLE_USER", profileDTO.getRole(), "역할이 일치해야 합니다.");
////        Assertions.assertEquals(true, profileDTO.getPolicyAgreed(), "약관 동의 상태는 true여야 합니다.");
////        Assertions.assertEquals(false, profileDTO.getSubscribed(), "구독 상태는 false여야 합니다.");
////        Assertions.assertEquals("google", profileDTO.getProvider(), "소셜 제공자가 일치해야 합니다.");
////
////    }
//
//    @Test
//    void testGetUserProfileByEmail_UserNotFound() {
//        Assertions.assertThrows(IllegalArgumentException.class, () -> {
//            unifiedUserService.getUserProfileByEmail("notfound@exam.com");
//        }, "존재하지 않는 이메일에 대해 예외를 발생시켜야 합니다.");
//    }
//=======
//        Assertions.assertFalse(afterUnifiedUser.isPresent(), "unifieduser should be deleted");
//        Assertions.assertFalse(afterSocialUser.isPresent(), "socialUserShould be deleted");
//
//    }
//
//>>>>>>> 예찬
//
//
//}
