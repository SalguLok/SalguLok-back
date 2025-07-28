package com.salgulok.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Default
    ERROR(400, "요청 처리에 실패했습니다."),
    UNAUTHORIZED_ACCESS(401, "인증되지 않은 사용자입니다."),

    // auth
    KAKAO_LOGIN_ERROR(401, "카카오 로그인 인증에 실패했습니다."),
    REFRESH_TOKEN_EMPTY(400, "리프레시 토큰이 존재하지 않습니다."),
    ACCESS_TOKEN_EMPTY(401, "엑세스 토큰이 존재하지 않습니다."),
    ACCESS_TOKEN_INVALID(401, "엑세스 토큰이 유효하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(401, "엑세스 토큰이 만료됐습니다."),
    USER_NOT_FOUND(404, "존재하는 사용자가 없습니다.");

    private final int status;
    private final String message;
}
