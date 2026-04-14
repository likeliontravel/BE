package org.example.be.legacy.unifieduser.repository;

import java.util.Optional;

import org.example.be.legacy.unifieduser.entity.UnifiedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnifiedUserRepository extends JpaRepository<UnifiedUser, Long> {

	public Optional<UnifiedUser> findByUserIdentifier(String userIdentifier);
}
