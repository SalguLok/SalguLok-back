package com.salgulok.auth.service.jwt;

import com.salgulok.auth.service.principal.CustomUserDetails;
import com.salgulok.auth.service.principal.CustomUserDetailsService;
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
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest,
                                    HttpServletResponse servletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorization = servletRequest.getHeader("Authorization");
            if(authorization != null){
                String token = jwtManager.substringToken(authorization);
                String userId = jwtManager.getUserIdFromClaims(token);

                CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(userId);

                Authentication auth = new UsernamePasswordAuthenticationToken(customUserDetails.getUser(), null, customUserDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (ExpiredJwtException e) {
            // 토큰 만료 에러
            throw new SalgulokException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 변조 등으로 인한 토큰 유효성 에러
            throw new SalgulokException(ErrorCode.ACCESS_TOKEN_INVALID);
        }

        // 다음 필터 계속 실행
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
