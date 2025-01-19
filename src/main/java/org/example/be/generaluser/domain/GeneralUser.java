package org.example.be.generaluser.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.unifieduser.entity.UnifiedUser;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "general_user")
public class GeneralUser {

    // General User table id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // General User 이메일
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    // General User 비밀번호
    @Column(nullable = false, length = 300)
    private String password;

    // General User 이름
    @Column(nullable = false, length = 100)
    private String name;

    // General User 권한
    @Column(nullable = false)
    private String role;
}
