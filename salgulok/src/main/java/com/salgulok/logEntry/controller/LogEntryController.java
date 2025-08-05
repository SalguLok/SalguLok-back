package com.salgulok.logEntry.controller;

import com.salgulok.logEntry.dto.request.LogEntryCreateRequest;
import com.salgulok.logEntry.service.LogEntryService;
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> createLogEntry(@AuthenticationPrincipal User user,
                                               @RequestBody LogEntryCreateRequest request) {
        logEntryService.createLogEntry(user, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/log-entries/{entryId}")
    public ResponseEntity<Void> updateLogEntry(
            @AuthenticationPrincipal User user,              // 로그인한 사용자 정보
            @PathVariable Long entryId,                      // 수정할 하루 기록 ID
            @RequestBody LogEntryUpdateRequest request       // 수정할 템플릿 리스트
    ) {
        logEntryService.updateLogEntry(user, entryId, request); // 서비스 호출
        return ResponseEntity.ok().build();                     // 응답 200 OK
    }

    @DeleteMapping("/log-entries/{entryId}")
    public ResponseEntity<Void> deleteLogEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long entryId
    ) {
        logEntryService.deleteLogEntry(user, entryId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}
