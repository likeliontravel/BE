package org.example.be.unifieduser.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LinkSocialDTO {
    private String userIdentifier;
    private String socialProvider;  // 예: Google, Kakao 등
    private String socialIdentifier; // 예: Google 또는 Kakao의 사용자 ID
}
