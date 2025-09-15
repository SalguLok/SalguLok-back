package com.salgulok.logEntry.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class LogEntryDateListResponse {
    private Long logId;
    private List<Item> items; // 날짜 칩 렌더용

    @Getter @Builder
    public static class Item {
        private Long entryId;
        private LocalDate entryDate;
        private String thumbnailUrl;   // 대표 이미지 1장(없으면 null)
        private Integer templateCount; // 옵션: 해당 날짜 템플릿 개수
    }
}