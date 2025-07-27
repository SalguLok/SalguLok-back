package com.salgulok.auth.service;

import com.salgulok.auth.domain.User;
import com.salgulok.auth.dto.request.KakaoCodeRequest;
import com.salgulok.auth.dto.response.JwtTokenResponse;
import com.salgulok.auth.dto.summary.KakaoUserInfo;
import com.salgulok.auth.repository.UserRepository;
import com.salgulok.auth.service.jwt.JwtTokenProvider;
import com.salgulok.auth.service.jwt.RefreshTokenService;
import com.salgulok.auth.service.kakao.KakaoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KakaoService kakaoService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Transactional
    public JwtTokenResponse kakaoLoginOrSignUp(KakaoCodeRequest request, HttpServletResponse response){
        String kakaoAccessToken = request.getCode();
        KakaoUserInfo userInfo = kakaoService.getKakaoIdFromAccessToken(kakaoAccessToken);
        Long kakaoId = userInfo.getKakaoId();

        // 존재하는 회원이 없으면 회원 저장 후 토큰 발급
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .kakaoId(kakaoId)
                            .build();
                    return userRepository.save(newUser);
                });

        return createTokens(user, response);
    }

    private JwtTokenResponse createTokens(User user, HttpServletResponse response){
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        refreshTokenService.save(user, refreshToken); // refresh token Redis에 저장
        return new JwtTokenResponse(accessToken, refreshToken);
    }
}
