package com.salgulok.auth.service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        // SalgulokException EntryPoint로 전달되면 프론트가 받도록.
        Throwable cause = authException.getCause();
        if (cause instanceof SalgulokException se) {
            response.setStatus(se.getErrorCode().getStatus());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(
                    new ErrorDto(
                            LocalDateTime.now().toString(),
                            se.getErrorCode().getStatus(),
                            se.getErrorCode().name(),
                            se.getErrorCode().getMessage(),
                            request.getRequestURI()
                    )
            ));
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}
