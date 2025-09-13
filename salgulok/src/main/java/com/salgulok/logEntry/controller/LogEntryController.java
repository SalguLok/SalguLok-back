package com.salgulok.logEntry.controller;

import com.salgulok.logEntry.dto.request.LogEntryCreateRequest;
import com.salgulok.logEntry.dto.request.LogEntryUpdateRequest;
import com.salgulok.logEntry.dto.response.LogEntryUpdateResponse;
import com.salgulok.logEntry.service.LogEntryService;
import com.salgulok.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/logs/{logId}/entries")
@RequiredArgsConstructor
public class LogEntryController {

    private final LogEntryService logEntryService;

    @PostMapping
    public ResponseEntity<Void> createLogEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long logId,
            @RequestBody @Valid LogEntryCreateRequest request
    ) {
        Long entryId = logEntryService.createLogEntry(user, logId, request);
        URI location = URI.create(String.format("/logs/%d/entries/%d", logId, entryId));
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<LogEntryUpdateResponse> updateLogEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long logId,
            @PathVariable Long entryId,
            @RequestBody @Valid LogEntryUpdateRequest request
    ) {
        LogEntryUpdateResponse response = logEntryService.updateLogEntry(user, logId, entryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteLogEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long logId,
            @PathVariable Long entryId
    ) {
        logEntryService.deleteLogEntry(user, logId, entryId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{entryId}/summary")
    public ResponseEntity<Void> saveSummary(
            @PathVariable Long logId,
            @PathVariable Long entryId,
            @RequestBody String summary
    ) {
        logEntryService.saveSummary(logId, entryId, summary);
        return ResponseEntity.ok().build();
    }
}