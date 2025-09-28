package com.salgulok.user.controller;

import com.salgulok.user.domain.User;
import com.salgulok.user.dto.request.UserInfoRequest;
import com.salgulok.user.dto.request.NicknameRequest;
import com.salgulok.user.dto.response.IsTravelingResponse;
import com.salgulok.user.dto.response.UserResponse;
import com.salgulok.user.dto.response.UsernameDuplicateResponse;
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

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateUserProfile(@AuthenticationPrincipal User user, @Valid @RequestBody UserInfoRequest request){
        UserResponse response = userService.updateUserProfile(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{nickname}")
    public ResponseEntity<UserResponse> getOtherInfo(@PathVariable("nickname") String nickname){
        return ResponseEntity.ok(userService.getOtherInfo(nickname));
    }

    @PostMapping("/info")
    public ResponseEntity<UserResponse> userInfoEnter(@AuthenticationPrincipal User user, @Valid @RequestBody UserInfoRequest request){
        UserResponse response = userService.createUserInfo(user, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/duplicate")
    public ResponseEntity<UsernameDuplicateResponse> checkUsernameDuplicate(@Valid @RequestBody NicknameRequest request){
        UsernameDuplicateResponse response = userService.checkUsernameDuplicate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/isTraveling")
    public ResponseEntity<IsTravelingResponse> checkIfIsTraveling(@AuthenticationPrincipal User user){
        IsTravelingResponse response = userService.checkIfTraveling(user);
        return ResponseEntity.ok(response);
    }
}
