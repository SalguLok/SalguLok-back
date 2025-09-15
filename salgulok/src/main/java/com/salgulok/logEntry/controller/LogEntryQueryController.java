package com.salgulok.logEntry.controller;

import com.salgulok.logEntry.dto.response.LogEntryDetailResponse;
import com.salgulok.logEntry.dto.response.LogEntryDateListResponse;
import com.salgulok.logEntry.service.LogEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.salgulok.logEntry.dto.response.FillCalendarResponse;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

import java.time.LocalDate;

@RestController
@RequestMapping("/logs/{logId}/entries")
@RequiredArgsConstructor
public class LogEntryQueryController {

    private final LogEntryService logEntryService;

    /**
     * 1) 특정 날짜 엔트리 + 템플릿(이미지 포함)
     *    엔트리가 없어도 200 + 빈 templates 반환
     */
    @GetMapping("/by-date")
    public ResponseEntity<LogEntryDetailResponse> getEntryByDate(
            @PathVariable Long logId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(logEntryService.getEntryByDateNullable(logId, date));
    }

    /**
     * 2) 날짜 칩 리스트(날짜 + 대표 이미지 1장)
     *    logId에 속한 모든 LogEntry를 오름차순으로 반환
     */
    @GetMapping("/dates")
    public ResponseEntity<LogEntryDateListResponse> getEntryDates(@PathVariable Long logId) {
        return ResponseEntity.ok(logEntryService.getEntryDatesWithThumbnail(logId));
    }

    @GetMapping("/fill-states")
    public ResponseEntity<FillCalendarResponse> getFillStates(
            @PathVariable Long logId,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(logEntryService.getFillCalendar(logId, start, end));
    }

    @GetMapping("/{entryId}/summary")
    public ResponseEntity<String> getSummary(
            @PathVariable Long logId,
            @PathVariable Long entryId
    ) {
        String summary = logEntryService.getSummary(logId, entryId);
        return ResponseEntity.ok(summary);
    }
}
