// dto/response/PostResponse.java
package com.salgulok.community.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.salgulok.community.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드 응답에서 생략
public class PostResponse {
    private Long id;
    private Long authorId;           // ★ 추가
    private String username;
    private String region;        // Region 이름 또는 코드
    private String topic;
    private String content;
    private Boolean authorStaying;
    private String authorProfileImg;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;


    public static PostResponse from(Post p) {
        var user = p.getAuthor();
        var regionName = user.getRegion() != null ? user.getRegion().getName() : null; // Region 엔티티에 맞게
        String avatar = user.getProfileImg(); // 이미 절대 URL이면 그대로

        return new PostResponse(
                p.getId(),
                user.getUserId(),
                user.getUsername(),
                regionName,
                p.getTopic() != null ? p.getTopic().name() : null,
                p.getContent(),
                Boolean.TRUE.equals(user.getIsTraveling()),
                avatar,
                p.getCreatedAt()
        );
    }
}
