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
public class LogEntryUpdateResponse {

    private Long entryId;
    private List<UpdatedTemplateInfo> updatedTemplates;

    @Getter
    @Builder
    public static class UpdatedTemplateInfo {
        private Long templateId;
        private Long placeId;
    }

    public static LogEntryUpdateResponse from(LogEntry logEntry, List<Template> updatedTemplates) {
        List<UpdatedTemplateInfo> templateInfos = updatedTemplates.stream()
                .map(template -> UpdatedTemplateInfo.builder()
                        .templateId(template.getTemplateId())
                        .placeId(template.getPlaceId())
                        .build())
                .collect(Collectors.toList());

        return new LogEntryUpdateResponse(logEntry.getLogEntryId(), templateInfos);
    }
}
