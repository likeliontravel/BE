package org.example.be.jwt.repository;

import org.example.be.jwt.domain.JWTBlackListToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JWTBlackListRepository extends JpaRepository<JWTBlackListToken, Integer> {

    Iterable<JWTBlackListToken> findAllByUserIdentifier(String userIdentifier);

    JWTBlackListToken findByAccessToken(String token);

    JWTBlackListToken findByRefreshToken(String token);
}
