/*
package org.example.be.Tourapi.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // id 필드는 자동 증가
    private Long id;

    @CreationTimestamp  // 엔티티 생성 시 자동으로 생성 시간 기록
    private LocalDateTime createdAt;

    @UpdateTimestamp  // 엔티티 수정 시 자동으로 수정 시간 기록
    private LocalDateTime updatedAt;
}

*/
