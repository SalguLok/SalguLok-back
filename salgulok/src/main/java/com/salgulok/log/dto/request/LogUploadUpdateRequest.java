package com.salgulok.log.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogUploadUpdateRequest {
    @NotNull
    private Boolean isUpload; // true: 게시, false: 게시 해제
}
