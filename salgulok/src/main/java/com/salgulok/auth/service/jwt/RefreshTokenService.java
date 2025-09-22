package com.salgulok.auth.service.jwt;

import com.salgulok.auth.domain.RefreshToken;
import com.salgulok.user.domain.User;
import com.salgulok.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;

    public void save(User user, String refreshToken) {
        LocalDateTime expiryDate = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtUtils.getRefreshTokenExpiration()));

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .expiryDate(expiryDate)
                .token(refreshToken)
                .build();
        refreshTokenRepository.save(token);
    }
}
