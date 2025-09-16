package com.salgulok.log.dto.response;

import com.salgulok.log.domain.Log;
import com.salgulok.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class LogResponse {
    private Long logId;
    private String writer;
    private String writerProfile;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isPublic;
    private Long regionId;
    private String imgUrl;
    private String oneReview;
    private Long likes;

    public static LogResponse from(Log log){
        return LogResponse.builder()
                .logId(log.getLogId())
                .writer(log.getUser().getUsername())
                .writerProfile(log.getUser().getProfileImg())
                .title(log.getTitle())
                .startDate(log.getStartDate())
                .endDate(log.getEndDate())
                .isPublic(log.getIsPublic())
                .regionId(log.getRegion().getRegionId())
                .imgUrl(log.getImgUrl())
                .oneReview(log.getOneReview())
                .likes(log.getLikes())

                .build();
    }
}
