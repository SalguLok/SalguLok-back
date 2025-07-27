package com.salgulok.auth.controller;

import com.salgulok.auth.dto.request.KakaoCodeRequest;
import com.salgulok.auth.dto.response.JwtTokenResponse;
import com.salgulok.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> kakaoLoginOrSignUp(@RequestBody @Valid KakaoCodeRequest request, HttpServletResponse response){
        JwtTokenResponse jwtTokenResponse = authService.kakaoLoginOrSignUp(request, response);
        return ResponseEntity.ok().body(jwtTokenResponse);
    }
}
