package com.salgulok.auth.service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgulok.global.exception.CustomAuthenticationException;
import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.global.exception.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED_ACCESS; // 기본값
        if (authException instanceof CustomAuthenticationException customEx) {
            errorCode = customEx.getErrorCode();
        }

        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                new ErrorDto(
                        LocalDateTime.now().toString(),
                        errorCode.getStatus(),
                        errorCode.name(),
                        errorCode.getMessage(),
                        request.getRequestURI()
                )
        ));
    }
}