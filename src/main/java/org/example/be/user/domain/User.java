package org.example.be.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User {

    // User table id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // User 이메일
    @Column(length = 100, unique = true)
    private String userEmail;

    // User 비밀번호
    @Column()
    private String userPwd;

    // User 이름
    @Column(length = 100)
    private String userName;

    // User 권한
    @Column()
    private String userRole;

    // User 이용약관 동의 : 기본 False
    @Column(nullable = false)
    private Boolean userPolicy = false;

    // User 구독 여부 : 기본 False
    @Column(nullable = false)
    private Boolean userSubscribe = false;

    // 소셜 로그인 여부
    @Column(nullable = false)
    private Boolean social = false;
}
