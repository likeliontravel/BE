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

    // 생성 시각
    @CreationTimestamp
    @Column(updatable = false) // 생성 시각은 자동 초기화 후 변경 불가
    private LocalDateTime createdTime;

    // 업데이트 시각 ( 또는 수정 시각 )
    @Column
    @UpdateTimestamp    // 업데이트 시각은 자동 변경
    private LocalDateTime updatedTime;

}
