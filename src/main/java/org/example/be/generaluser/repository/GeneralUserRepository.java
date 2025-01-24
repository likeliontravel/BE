package org.example.be.generaluser.repository;

import org.example.be.generaluser.domain.GeneralUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneralUserRepository extends JpaRepository<GeneralUser, Long> {

//    Optional<GeneralUser> findByEmail(String email);

    Optional<GeneralUser> findByUserIdentifier(String userIdentifier);

    Optional<GeneralUser> findByEmail(String email);
}
