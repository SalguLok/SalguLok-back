package com.salgulok.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KakaoCodeRequest {
    @NotBlank(message = "카카오톡 인가코드가 비어있습니다.")
    private final String code;
}
