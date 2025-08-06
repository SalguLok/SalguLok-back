package com.salgulok.user.controller;

import com.salgulok.user.domain.User;
import com.salgulok.user.dto.request.CreateUserInfoRequest;
import com.salgulok.user.dto.response.UserResponse;
import com.salgulok.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(userService.getMyInfo(user));
    }

    @PatchMapping("/info")
    public ResponseEntity<UserResponse> UserInfoEnter(@AuthenticationPrincipal User user, @Valid @RequestBody CreateUserInfoRequest request){
        UserResponse response = userService.createUserInfo(user, request);
        return ResponseEntity.noContent().build();
    }
}
