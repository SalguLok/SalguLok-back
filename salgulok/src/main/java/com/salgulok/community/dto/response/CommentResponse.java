// dto/response/CommentResponse.java
package com.salgulok.community.dto.response;

import com.salgulok.community.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long postId;
    private String username;
    private String content;

    public static CommentResponse from(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getPost().getId(),
                c.getAuthor().getUsername(),
                c.getContent()
        );
    }
}
