package org.example.be.jwt.service;

import lombok.RequiredArgsConstructor;
import org.example.be.jwt.domain.JWTBlackListToken;
import org.example.be.jwt.repository.JWTBlackListRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class JWTBlackListService {

    private final JWTBlackListRepository jwtBlackListRepository;

    public boolean isBlacklistedByEmail(String email, String accessToken, String refreshToken) {

        // 이메일로 블랙리스트에 등록된 토큰 가져오기
        Iterable<JWTBlackListToken> blackListTokens = jwtBlackListRepository.findAllByEmail(email);

        // 이메일로 찾은 모든 블랙리스트 토큰과 현재 토큰을 비교
        for (JWTBlackListToken token : blackListTokens) {

            if (token.getAccessToken().equals(accessToken) && token.getRefreshToken().equals(refreshToken)) {

                return true;
            }
        }

        return false;
    }

    //블랙리스트에 토큰 추가 (만료시간 저장)
    public void addToBlackList(String email, String accessToken, String refreshToken, Date expiredTime) {

        JWTBlackListToken blackListToken = new JWTBlackListToken();

        blackListToken.setEmail(email);
        blackListToken.setAccessToken(accessToken);
        blackListToken.setRefreshToken(refreshToken);

        jwtBlackListRepository.save(blackListToken);

        //만료 시간이 지나면 데이터 삭제 스케쥴링
        scheduleTokenRemoval(accessToken, expiredTime);
    }

    // 만료 시간 이후 블랙리스트 토큰 삭제 스케줄링
    private void scheduleTokenRemoval(String token, Date expiredTime) {

        long delay = expiredTime.getTime() - System.currentTimeMillis();

        Executors.newSingleThreadScheduledExecutor().schedule(() -> removeExpiredToken(token), delay, TimeUnit.MILLISECONDS);
    }

    //블랙리스트에서 만료된 토큰 삭제
    private void removeExpiredToken(String token) {

        JWTBlackListToken blackListToken = jwtBlackListRepository.findByAccessToken(token);

        if (blackListToken != null) {

            jwtBlackListRepository.delete(blackListToken);
        }
    }
}

