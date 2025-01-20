package org.example.be.unifieduser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.generaluser.domain.GeneralUser;
import org.example.be.oauth.entity.SocialUser;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "unified_user")
public class UnifiedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 우리 서비스에서 이용할 통합 유저테이블 식별자
    @Column(nullable = false, unique = true)
    private String userIdentifier;  // provider + " " + email 형식

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private Boolean policyAgreed = false;   // 이용약관 동의여부 ; 기본값 false

    @Column(nullable = false)
    private Boolean subscribed = false;     // 유료구독 가입여부 ; 기본값 false

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "general_user_id", referencedColumnName = "id")
    private GeneralUser generalUser;    // 일반유저에서 데이터 가져와 저장됨. 삭제 시 두 테이블 모두 해당 튜플 삭제(Cascade)

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "social_user_id", referencedColumnName = "id")
    private SocialUser socialUser;      // 소셜유저에서 데이터 가져와 저장됨. 삭제 시 두 테이블 모두 해당 튜플 삭제(Cascade)

}
