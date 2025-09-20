package com.salgulok.logEntry.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class LogEntryDetailResponse {
    private Long logId;
    private LocalDate entryDate;
    private Long entryId;                 // 없으면 null
    private Integer templateCount;        // templates.size()
    private List<TemplateSummary> templates;

    @Getter @Builder
    public static class TemplateSummary {
        private Long templateId;
        private Long placeId;
        private String text;
        private Integer star;
        private List<ImageSummary> images;
    }

    @Getter @Builder
    public static class ImageSummary {
        private Long imageId;
        private String objectKey;
        private String imageUrl;
    }
}