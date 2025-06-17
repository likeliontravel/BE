package org.example.be.security.util;

import org.example.be.exception.custom.SecurityAuthenticationException;
import org.example.be.exception.custom.UserAuthenticationNotFoundException;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.example.be.security.dto.UserContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// 인증된 사용자의 객체로부터 사용자의 정보를 가져오고 싶을 때 사용할 메서드를 모아놓을 클래스
public class SecurityUtil {
    public static String getUserIdentifierFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("[SecurityUtil] 인증 실패 - 인증 객체가 없거나 인증되지 않음");
            throw new SecurityAuthenticationException("인증되지 않은 사용자입니다.");
        }

        Object principal = authentication.getPrincipal();
        System.out.println("[SecurityUtil] principal 객체: " + principal);

        String userIdentifier = null;

        if (principal instanceof UserContext) {
            userIdentifier = ((UserContext) principal).getGeneralUserDTO().getUserIdentifier();
        } else if (principal instanceof CustomOAuth2User) {
            userIdentifier = ((CustomOAuth2User) principal).getUserIdentifier();
        }

        if (userIdentifier == null) {
            System.out.println("[SecurityUtil] 인증 객체에서 userIdentifier 추출 실패");
            throw new UserAuthenticationNotFoundException("인증 객체로부터 userIdentifier를 찾을 수 없습니다.");
        }

        System.out.println("[SecurityUtil] 추출된 userIdentifier: " + userIdentifier);
        return userIdentifier;
    }
}
