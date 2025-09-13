package com.salgulok.logEntry.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 하루 기록 저장 요청 DTO
 * - 하루 날짜 + 템플릿 목록을 담는다
 */

@Getter
@NoArgsConstructor

public class LogEntryCreateRequest {

//    /** 연관된 한 달 살구록 ID */
//    @NotNull(message = "logId는 필수입니다.")
//    private Long logId;

    /** 하루 기록 날짜 */
    @NotNull(message = "entryDate는 필수입니다.")
    private LocalDate entryDate;

    /** 하루 안의 장소별 템플릿 리스트 */
    @NotNull(message = "templates는 비어 있을 수 없습니다.")
    private List<TemplateCreateRequest> templates;
}
