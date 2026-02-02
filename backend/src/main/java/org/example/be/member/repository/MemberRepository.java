package org.example.be.member.repository;

import java.util.Optional;

import org.example.be.member.entity.Member;
import org.example.be.member.type.OauthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByEmail(String email);

	boolean existsByEmail(String email);

	Optional<Member> findByProviderAndEmail(OauthProvider provider, String email);
}
