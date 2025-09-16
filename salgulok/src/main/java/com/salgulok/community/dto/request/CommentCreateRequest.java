// dto/request/CommentCreateRequest.java
package com.salgulok.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CommentCreateRequest {
    @NotNull private Long authorId;
    @NotBlank private String content;
}
