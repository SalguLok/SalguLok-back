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
    USER_NOT_FOUND(404, "존재하는 사용자가 없습니다."),
    OWNER_MISMATCH(400, "수정 및 삭제 권한이 존재하지 않습니다."),

    // region
    REGION_NOT_FOUND(404, "존재하는 지역코드가 없습니다."),

    // salgulog
    SALGULOG_NOT_FOUND(404, "존재하는 살구로그가 없습니다."),
    INVALID_DATE_RANGE(400, "시작날짜는 종료일보다 이전이어야 합니다.");

    private final int status;
    private final String message;
}
