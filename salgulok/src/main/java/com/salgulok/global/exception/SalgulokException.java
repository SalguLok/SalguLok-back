package com.salgulok.global.exception;

import lombok.Getter;

@Getter
public class SalgulokException extends RuntimeException{
    private final ErrorCode errorCode;

    public SalgulokException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public SalgulokException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getMessage() + " - " + detailMessage);
        this.errorCode = errorCode;
    }

}
