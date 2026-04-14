package org.example.be.legacy.generaluser.repository;

import java.util.Optional;

import org.example.be.legacy.generaluser.domain.GeneralUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneralUserRepository extends JpaRepository<GeneralUser, Long> {

	Optional<GeneralUser> findByUserIdentifier(String userIdentifier);

}
