package com.salgulok.log.dto.summary;

import com.salgulok.log.domain.Log;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class LogSummary {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isPublic;
    private Long regionId;
    private String imgUrl;
    private String oneReview;

    public static LogSummary from(Log log){
        return LogSummary.builder()
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
