package com.salgulok.auth.service.jwt;

import com.salgulok.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public String createAccessToken(User user) {
        return createToken(user.getUserId(), jwtProperties.getAccessTokenExpiration());
    }

    public String createRefreshToken(User user) {
        return createToken(user.getUserId(), jwtProperties.getRefreshTokenExpiration());
    }

    private String createToken(Long userId, long expiration) {
        Claims claims = Jwts.claims().setSubject(userId.toString());
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(jwtProperties.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
