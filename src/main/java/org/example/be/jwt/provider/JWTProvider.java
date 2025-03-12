package org.example.be.jwt.provider;

import lombok.RequiredArgsConstructor;
import org.example.be.generaluser.domain.GeneralUser;
import org.example.be.generaluser.repository.GeneralUserRepository;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.example.be.oauth.entity.SocialUser;
import org.example.be.oauth.repository.SocialUserRepository;
import org.example.be.security.service.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JWTProvider {

    private final CustomUserDetailsService customUserDetailsService;
    private final GeneralUserRepository generalUserRepository;
    private final SocialUserRepository socialUserRepository;

    //유저 인증객체 생성
    public Authentication getUserDetails(String userIdentifier) {

        Optional<GeneralUser> generalUser = generalUserRepository.findByUserIdentifier(userIdentifier);
        if (generalUser.isPresent()) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userIdentifier);
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } else {
            Optional<SocialUser> socialUserOptional = socialUserRepository.findByUserIdentifier(userIdentifier);
            if (socialUserOptional.isPresent()) {
                SocialUser socialUser = socialUserOptional.get();
                Map<String, Object> attributes = new HashMap<>();

                OAuth2User oAuth2User = new CustomOAuth2User(socialUser, attributes);

                return new UsernamePasswordAuthenticationToken(oAuth2User, null, oAuth2User.getAuthorities());
            } else {
                throw new IllegalArgumentException(userIdentifier + " 해당 userIdnetifier를 찾을 수 없습니다.");
            }
        }
    }
}
