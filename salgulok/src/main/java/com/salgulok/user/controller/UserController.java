package com.salgulok.user.controller;

import com.salgulok.user.domain.User;
import com.salgulok.user.dto.response.UserInfoResponse;
import com.salgulok.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> UserInfo(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(userService.getUserInfo(user));
    }
}
