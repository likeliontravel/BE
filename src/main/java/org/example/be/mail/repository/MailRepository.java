package org.example.be.mail.repository;

import org.example.be.mail.domain.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailRepository extends JpaRepository<Mail, Integer> {

    void deleteByEmail(String email);

    Optional<Mail> findByEmail(String email);
}
