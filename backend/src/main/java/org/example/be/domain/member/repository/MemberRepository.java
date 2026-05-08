package org.example.be.domain.member.repository;

import java.util.Optional;

import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.type.OauthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByEmail(String email);

	boolean existsByEmail(String email);

	// CustomOAuth2UserService.loadUser() 에서 이메일 단독 검증으로 변경 후 미사용 중
	Optional<Member> findByEmailAndOauthProvider(String email, OauthProvider provider);

}
