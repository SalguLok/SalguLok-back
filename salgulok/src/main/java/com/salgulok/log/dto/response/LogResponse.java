package com.salgulok.log.dto.response;

import com.salgulok.log.domain.Log;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class LogResponse {
    private Long logId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isPublic;
    private Long regionId;
    private String imgUrl;
    private String oneReview;

    public static LogResponse from(Log log){
        return LogResponse.builder()
                .logId(log.getLogId())
                .title(log.getTitle())
                .startDate(log.getStartDate())
                .endDate(log.getEndDate())
                .isPublic(log.getIsPublic())
                .regionId(log.getRegion().getRegionId())
                .imgUrl(log.getImgUrl())
                .oneReview(log.getOneReview())
                .build();
    }
}
