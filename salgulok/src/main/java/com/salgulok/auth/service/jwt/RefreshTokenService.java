package com.salgulok.auth.service.jwt;

import com.salgulok.auth.domain.RefreshToken;
import com.salgulok.auth.domain.User;
import com.salgulok.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    private final long REFRESH_TOKEN_TTL = 31L; // 31일 뒤 redis에서 제거

    public void save(User user, String refreshToken) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(REFRESH_TOKEN_TTL);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .expiryDate(expiryDate)
                .token(refreshToken)
                .build();
        refreshTokenRepository.save(token);
    }
}
