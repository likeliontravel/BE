package org.example.be.unifieduser.repository;

import org.example.be.unifieduser.entity.UnifiedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnifiedUserRepository extends JpaRepository<UnifiedUser, Long> {

    public Optional<UnifiedUser> findByUserIdentifier(String userIdentifier);

    public Optional<UnifiedUser> findByEmail(String email);   // 기존 이메일 식별 방식의 로직이 있을까봐 임시로 만들어두었습니다. 기능 이상없음 확인 되면 삭제 예정.

}
