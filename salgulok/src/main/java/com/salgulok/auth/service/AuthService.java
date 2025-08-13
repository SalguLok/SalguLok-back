package com.salgulok.auth.service;

import com.salgulok.auth.dto.response.LoginResponse;
import com.salgulok.auth.service.jwt.JwtUtils;
import com.salgulok.user.domain.User;
import com.salgulok.auth.dto.request.KakaoCodeRequest;
import com.salgulok.auth.dto.response.JwtTokenResponse;
import com.salgulok.auth.dto.summary.KakaoUserInfo;
import com.salgulok.user.repository.UserRepository;
import com.salgulok.auth.service.jwt.JwtManager;
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
    private final JwtManager jwtManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse kakaoLoginOrSignUp(KakaoCodeRequest request, HttpServletResponse response){
        String kakaoAccessToken = request.getCode();
        KakaoUserInfo userInfo = kakaoService.getKakaoIdFromAccessToken(kakaoAccessToken);

        Long kakaoId = userInfo.getKakaoId();
        User user = userRepository.findByKakaoId(kakaoId).orElse(null);
        boolean isNewUser = false;

        // 존재하는 회원이 없으면 회원 저장 후 토큰 발급
        if (user == null) {
            isNewUser = true;
            user = userRepository.save(User.builder()
                    .kakaoId(kakaoId)
                    .build());
        }

        JwtTokenResponse token = createTokens(user, response);
        return new LoginResponse(token, isNewUser);
    }

    private JwtTokenResponse createTokens(User user, HttpServletResponse response){
        String accessToken = jwtManager.createAccessToken(user);
        String refreshToken = jwtManager.createRefreshToken(user);

        refreshTokenService.saveToken(user.getUserId(), refreshToken, jwtUtils.getRefreshTokenExpiration()); // refresh token Redis에 저장
        return new JwtTokenResponse(accessToken, refreshToken);
    }
}
