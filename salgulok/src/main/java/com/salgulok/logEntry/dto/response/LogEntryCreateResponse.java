package com.salgulok.logEntry.dto.response;

import com.salgulok.logEntry.domain.LogEntry;
import com.salgulok.logEntry.domain.Template;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class LogEntryCreateResponse {
    private Long entryId;
    private List<TemplateInfo> templates;

    @Getter
    @Builder
    public static class TemplateInfo {
        private Long templateId;
        private Long placeId;
    }

    // 정적 팩토리 메서드
    public static LogEntryCreateResponse from(LogEntry logEntry, List<Template> templates) {
        List<TemplateInfo> templateInfos = templates.stream()
                .map(template -> TemplateInfo.builder()
                        .templateId(template.getTemplateId())
                        .placeId(template.getPlaceId())
                        .build())
                .collect(Collectors.toList());

        return new LogEntryCreateResponse(logEntry.getLogEntryId(), templateInfos);
    }
}
