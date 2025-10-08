package com.salgulok.logEntry.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class LogEntryCreateResponse {
    private Long entryId;
    private LocalDate entryDate;
    private List<TemplateResponse> templates;
}