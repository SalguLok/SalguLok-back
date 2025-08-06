package com.salgulok.user.dto.response;

import com.salgulok.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserInfoResponse {
    private String nickname;
    private String intro;
    private String profileImg;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .nickname(user.getUsername())
                .intro(user.getIntro())
                .profileImg(user.getProfileImg())
                .build();
    }
}
