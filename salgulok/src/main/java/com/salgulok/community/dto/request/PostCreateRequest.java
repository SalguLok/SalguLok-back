// dto/request/PostCreateRequest.java
package com.salgulok.community.dto.request;

import com.salgulok.community.domain.Topic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostCreateRequest {
    @NotNull private Topic topic;
    @NotBlank private String content;
    @NotNull private Long regionId;
    @NotNull private Long authorId; // 로그인 처리했다면 Security에서 꺼내도 됨
}
