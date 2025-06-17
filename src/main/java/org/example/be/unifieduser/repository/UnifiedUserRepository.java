package org.example.be.unifieduser.repository;

import org.example.be.unifieduser.entity.UnifiedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnifiedUserRepository extends JpaRepository<UnifiedUser, Long> {

    public Optional<UnifiedUser> findByUserIdentifier(String userIdentifier);
}
