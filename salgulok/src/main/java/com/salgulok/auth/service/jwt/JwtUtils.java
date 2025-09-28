package com.salgulok.auth.service.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
@Getter
public class JwtUtils {
    private final String secretKey;
    private final long accessTokenMillis = 1000L * 60 * 60 * 24; // 개발 중임으로 우선 하루. 배포 시 1시간으로 수정 예정
    private final long refreshTokenMillis = 1000L * 60 * 60 * 24 * 31; // 1달(31일)
    private final int refreshTokenSeconds = 60 * 60 * 24 * 31; // 31일 초단위

    public JwtUtils(@Value("${jwt.secret.key}") String secretKey) {
        this.secretKey = secretKey;
    }

    public Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

}