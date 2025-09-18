package com.salgulok.log.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogCommentCreateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    public LogCommentCreateRequest(String content) {
        this.content = content;
    }
}