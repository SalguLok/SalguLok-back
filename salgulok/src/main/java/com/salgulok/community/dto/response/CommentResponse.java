// dto/response/CommentResponse.java
package com.salgulok.community.dto.response;

import com.salgulok.community.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long authorId;
    private Long postId;
    private String authorProfileImg;
    private String username;
    private String content;

    public static CommentResponse from(Comment c) {
        var user = c.getAuthor();
        String avatar = user.getProfileImg();

        return new CommentResponse(
                c.getId(),
                user.getUserId(),
                c.getPost().getId(),
                avatar,
                c.getAuthor().getUsername(),
                c.getContent()
        );
    }
}
