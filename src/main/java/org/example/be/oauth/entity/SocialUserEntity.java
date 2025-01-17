package org.example.be.oauth.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "social_user")
public class SocialUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String email;   // 사용자 이메일

    @Column(nullable = false)
    private String name;    // 사용자 이름

    @Column(nullable = false)
    private String provider;    // 소셜 제공자

    @Column(nullable = false)
    private String providerId;  // 소셜 제공자가 제공하는 유저 고유 식별자

    @Column(nullable = false)
    private String role;

//    @OneToOne
//    @JoinColumn(name = "unified_user_id", nullable = false)
//    private UnifiedUser unifiedUser;  // 통합 이후 주석해제
}
