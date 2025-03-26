package org.example.be.config;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 시간 정보를 받아올 때 사용하는 클래스. 엔티티에서 사용하면 생성시각 정보가 컬럼으로 함께 저장됨. (수정 시 시각도 자동)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class Base {
    @CreationTimestamp
    //생성일
    @Column(updatable = false) //생성 시각을 변하지 않게 생성
    private LocalDateTime createdTime;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedTime;

}
