package com.salgulok.logEntry.dto.request;

import java.util.List;

/**
 * 하루 기록(=LogEntry)의 수정 요청 DTO
 * → 여러 개의 템플릿 정보들을 담아서 한 번에 수정
 */
public class LogEntryUpdateRequest {
    private List<TemplateUpdateRequest> templates;

    // Getter, Setter
    public List<TemplateUpdateRequest> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateUpdateRequest> templates) {
        this.templates = templates;
    }
}
