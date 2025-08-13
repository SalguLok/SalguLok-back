package com.salgulok.auth.service.jwt;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtManager {

    private final JwtUtils jwtUtils;

    public String createAccessToken(Long userId) {
        return createToken(userId, jwtUtils.getAccessTokenMillis());
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, jwtUtils.getRefreshTokenMillis());
    }

    private String createToken(Long userId, long expiration) {
        Claims claims = Jwts.claims().setSubject(userId.toString());
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(jwtUtils.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String substringToken(String token){
        // 토큰 존재하고 "Bearer "로 시작하는지 확인
        if(StringUtils.hasText(token) && token.startsWith("Bearer ")){
            return token.substring(7);
        }
        throw new SalgulokException(ErrorCode.ACCESS_TOKEN_INVALID);
    }

    // JWT 파싱후 Claims에서 userId 추출
    public String getUserIdFromClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtUtils.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
