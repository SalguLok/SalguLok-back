package com.salgulok.log.dto.response;

import com.salgulok.log.domain.LogComment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LogCommentResponse {

    private Long id;
    private Long logId;
    private Long authorId;
    private String authorName;
    private String authorProfileImg;
    private String content;
    private LocalDateTime createdAt;

    public LogCommentResponse(LogComment logComment) {
        this.id = logComment.getId();
        this.logId = logComment.getLog().getLogId();
        this.authorId = logComment.getAuthor().getUserId();
        this.authorName = logComment.getAuthor().getUsername();
        this.authorProfileImg = logComment.getAuthor().getProfileImg();
        this.content = logComment.getContent();
        this.createdAt = logComment.getCreatedAt();
    }
}