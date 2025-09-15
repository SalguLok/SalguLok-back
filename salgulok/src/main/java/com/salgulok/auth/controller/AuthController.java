package com.salgulok.auth.controller;

import com.salgulok.auth.dto.request.KakaoCodeRequest;
import com.salgulok.auth.dto.response.JwtTokenResponse;
import com.salgulok.auth.dto.response.LoginResponse;
import com.salgulok.auth.dto.response.ReissueResponse;
import com.salgulok.auth.service.AuthService;
import com.salgulok.user.domain.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> kakaoLoginOrSignUp(@RequestBody @Valid KakaoCodeRequest request,
                                                            HttpServletResponse response){
        LoginResponse loginResponse = authService.kakaoLoginOrSignUp(request, response);
        return ResponseEntity.ok().body(loginResponse);
    }

    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                                   HttpServletResponse response){
        ReissueResponse reissueResponse = authService.reissue(refreshToken, response);
        return ResponseEntity.ok().body(reissueResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                       @AuthenticationPrincipal User user,
                                       HttpServletResponse response){
        authService.logout(user, accessToken, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok().body("hello");
    }
}
