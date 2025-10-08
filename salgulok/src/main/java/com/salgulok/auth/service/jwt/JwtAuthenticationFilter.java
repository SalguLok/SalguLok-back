package com.salgulok.auth.service.jwt;

import com.salgulok.auth.service.principal.CustomUserDetails;
import com.salgulok.auth.service.principal.CustomUserDetailsService;
import com.salgulok.global.exception.CustomAuthenticationException;
import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtManager jwtManager;
    private final TokenService tokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null) {
            try {
                handleAuthorizationHeader(request, response, authorization);
            } catch (CustomAuthenticationException ex) {
                authenticationEntryPoint.commence(request, response, ex);
                return;
            }
        }

        // 정상 요청만 다음 필터로 전달
        filterChain.doFilter(request, response);
    }

    private void handleAuthorizationHeader(HttpServletRequest request,
                                           HttpServletResponse response,
                                           String authorization) {
        String accessToken = jwtManager.substringToken(authorization);

        checkBlacklist(accessToken, request, response);

        String userId = getUserIdFromToken(accessToken);

        // @AuthenticationPrincipal에 User 주입
        CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(userId);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails.getUser(), null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void checkBlacklist(String accessToken,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        if (tokenService.isAccessTokenBlacklisted(accessToken)) {
            throw new CustomAuthenticationException(ErrorCode.ACCESS_TOKEN_IN_BLACKLIST);
        }
    }

    private String getUserIdFromToken(String accessToken) {
        try {
            return jwtManager.getUserIdFromClaims(accessToken);
        } catch (ExpiredJwtException e) {
            // 토큰 만료 에러
            throw new CustomAuthenticationException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 변조 등으로 인한 토큰 유효성 에러
            throw new CustomAuthenticationException(ErrorCode.ACCESS_TOKEN_INVALID);
        }
    }
}
