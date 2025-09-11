package com.salgulok.logEntry.controller;

import com.salgulok.logEntry.dto.request.LogEntryCreateRequest;
import com.salgulok.logEntry.dto.request.LogEntryUpdateRequest;
import com.salgulok.logEntry.dto.request.PlaceRatingRequest;
import com.salgulok.logEntry.dto.response.LogEntryUpdateResponse;
import com.salgulok.logEntry.service.LogEntryService;
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.salgulok.logEntry.dto.response.LogEntryCreateResponse;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;



@RestController
@RequestMapping("/log-entries")
@RequiredArgsConstructor
public class LogEntryController {

    private final LogEntryService logEntryService;

    /**
     * 하루 기록 저장 API
     *
     * @param user 로그인된 사용자
     * @param request 하루 기록 저장 요청 DTO
     * @return 성공 시 201 Created 응답
     */
    @PostMapping
    public ResponseEntity<LogEntryCreateResponse> createLogEntry(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid LogEntryCreateRequest request
    ) {
        LogEntryCreateResponse response = logEntryService.createLogEntry(user, request);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{entryId}")
    public ResponseEntity<LogEntryUpdateResponse> updateLogEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long entryId,
            @RequestBody @Valid LogEntryUpdateRequest request
    ) {
        LogEntryUpdateResponse response = logEntryService.updateLogEntry(user, entryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteLogEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long entryId
    ) {
        logEntryService.deleteLogEntry(user, entryId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @DeleteMapping("/templates/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @PathVariable Long templateId
    ) {
        logEntryService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build(); // 204
    }

    //장소 별점 필요한거 일단 주석 처리
    @PostMapping("/place-ratings")
    public ResponseEntity<Void> savePlaceRating(
            @RequestBody PlaceRatingRequest request
    ) {
        logEntryService.savePlaceRating(request);
        return ResponseEntity.ok().build(); // 200 OK
    }

    @PutMapping("{entryId}/summary")
    public ResponseEntity<Void> saveSummary(
            @PathVariable Long entryId,
            @RequestBody String summary
    ) {
        logEntryService.saveSummary(entryId, summary);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{entryId}/summary")
    public ResponseEntity<String> getSummary(
            @PathVariable Long entryId
    ) {
        String summary = logEntryService.getSummary(entryId);
        return ResponseEntity.ok(summary);
    }

}
