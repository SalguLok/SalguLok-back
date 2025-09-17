// dto/response/PostResponse.java
package com.salgulok.community.dto.response;

import com.salgulok.community.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String username;
    private String region;        // Region 이름 또는 코드
    private String topic;
    private String content;
    private Boolean authorStaying;

    public static PostResponse from(Post p) {
        var user = p.getAuthor();
        var regionName = user.getRegion() != null ? user.getRegion().getName() : null; // Region 엔티티에 맞게
        return new PostResponse(
                p.getId(),
                user.getUsername(),
                regionName,
                p.getTopic() != null ? p.getTopic().name() : null,
                p.getContent(),
                Boolean.TRUE.equals(user.getIsTraveling())
        );
    }
}
