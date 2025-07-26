package com.salgulok.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SalgulokException extends RuntimeException{
    private final ErrorCode errorCode;
}
