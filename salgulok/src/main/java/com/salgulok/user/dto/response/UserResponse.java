package com.salgulok.user.dto.response;

import com.salgulok.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponse {
    private String nickname;
    private String intro;
    private String profileImg;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .nickname(user.getUsername())
                .intro(user.getIntro())
                .profileImg(user.getProfileImg())
                .build();
    }
}
