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
import com.salgulok.auth.service.jwt.TokenService;
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
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse kakaoLoginOrSignUp(KakaoCodeRequest request, HttpServletResponse response){
        String authorizationCode = request.getCode();
        KakaoUserInfo userInfo = kakaoService.getUserInfoFromAccessToken(authorizationCode);

        Long kakaoId = userInfo.getKakaoId();
        User user = userRepository.findByKakaoId(kakaoId).orElse(null);
        boolean isNewUser = false;

        // 존재하는 회원이 없으면 회원 저장 후 토큰 발급
        if (user == null) {
            isNewUser = true;
            user = userRepository.save(User.builder()
                    .kakaoId(kakaoId)
                    .build());
        } else {
            if (user.getUsername() == null || user.getUsername().isEmpty()){
                isNewUser = true;
            }
        }

        String accessToken = createTokens(user, response);
        return new LoginResponse(accessToken, isNewUser, user.getUserId());
    }

    public ReissueResponse reissue(String cookieRefreshToken, HttpServletResponse response) {
        try {
            // jwt 만료 체크 포함 -> 만료시 ExpiredJwtException 에러로 걸림
            Long userId = Long.parseLong(jwtManager.getUserIdFromClaims(cookieRefreshToken));

            // Redis의 refreshToken과 일치하는지 확인
            String redisRefreshToken = tokenService.getToken(userId);
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

    public void logout(User user, String accessToken, HttpServletResponse response) {
        tokenService.deleteToken(user.getUserId());

        accessToken = jwtManager.substringToken(accessToken);
        long expireMillis = jwtManager.getExpiration(accessToken); // 남은 만료 시간(ms) redis TTL 설정
        tokenService.saveBlacklist(accessToken, expireMillis);

        // 저장된 쿠키 삭제
        Cookie deleteCookie = createRefreshTokenCookie(null);
        response.addCookie(deleteCookie);
    }

    // accessToken 및 refreshToken 생성
    private String createTokens(User user, HttpServletResponse response){
        String accessToken = jwtManager.createAccessToken(user.getUserId());
        String refreshToken = jwtManager.createRefreshToken(user.getUserId());

        tokenService.saveToken(user.getUserId(), refreshToken, jwtUtils.getRefreshTokenMillis()); // refresh token Redis에 저장

        // 클라이언트 쿠키에 Refresh Token 저장
        response.addCookie(createRefreshTokenCookie(refreshToken));

        return accessToken;
    }

    // refresh Token용 쿠키 설정
    private Cookie createRefreshTokenCookie(String refreshToken){
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);    // JS에서 접근 불가
        refreshCookie.setSecure(true);      // HTTPS 전용
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(jwtUtils.getRefreshTokenSeconds());

        return refreshCookie;
    }
}
