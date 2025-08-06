package com.salgulok.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateUserInfoRequest {
    @NotNull(message = "닉네임을 입력해주세요.")
    private String username;

    private String intro;
    private String profileImg;
}
