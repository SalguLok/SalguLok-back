package com.salgulok.auth.controller;

import com.salgulok.auth.dto.request.KakaoCodeRequest;
import com.salgulok.auth.dto.response.LoginResponse;
import com.salgulok.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> kakaoLoginOrSignUp(@RequestBody @Valid KakaoCodeRequest request, HttpServletResponse response){
        LoginResponse loginResponse = authService.kakaoLoginOrSignUp(request, response);
        return ResponseEntity.ok().body(loginResponse);
    }
}
