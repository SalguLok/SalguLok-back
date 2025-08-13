package com.salgulok.auth.service;

import com.salgulok.auth.dto.response.LoginResponse;
import com.salgulok.auth.dto.response.ReissueResponse;
import com.salgulok.auth.service.jwt.JwtUtils;
import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.user.domain.User;
import com.salgulok.auth.dto.request.KakaoCodeRequest;
import com.salgulok.auth.dto.summary.KakaoUserInfo;
import com.salgulok.user.repository.UserRepository;
import com.salgulok.auth.service.jwt.JwtManager;
import com.salgulok.auth.service.jwt.RefreshTokenService;
import com.salgulok.auth.service.kakao.KakaoService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
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

        String accessToken = createTokens(user, response);
        return new LoginResponse(accessToken, isNewUser);
    }

    private String createTokens(User user, HttpServletResponse response){
        String accessToken = jwtManager.createAccessToken(user.getUserId());
        String refreshToken = jwtManager.createRefreshToken(user.getUserId());

        refreshTokenService.saveToken(user.getUserId(), refreshToken, jwtUtils.getRefreshTokenMillis()); // refresh token Redis에 저장

        // 클라이언트 쿠키에 Refresh Token 저장
        response.addCookie(createRefreshTokenCookie(refreshToken));

        return accessToken;
    }

    private Cookie createRefreshTokenCookie(String refreshToken){
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);    // JS에서 접근 불가
        refreshCookie.setSecure(true);      // HTTPS 전용
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(jwtUtils.getRefreshTokenSeconds());

        return refreshCookie;
    }

    public ReissueResponse reissue(String cookieRefreshToken, HttpServletResponse response) {
        try {
            // jwt 만료 체크 포함 -> 만료시 ExpiredJwtException 에러로 걸림
            Long userId = Long.parseLong(jwtManager.getUserIdFromClaims(cookieRefreshToken));

            // Redis의 refreshToken과 일치하는지 확인
            String redisRefreshToken = refreshTokenService.getToken(userId);
            if (redisRefreshToken == null || !redisRefreshToken.equals(cookieRefreshToken)) {
                throw new SalgulokException(ErrorCode.REFRESH_TOKEN_INVALID);
            }

            String accessToken = jwtManager.createAccessToken(userId);
            return new ReissueResponse(accessToken);
        } catch (ExpiredJwtException e) {
            throw new SalgulokException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new SalgulokException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
    }
}
