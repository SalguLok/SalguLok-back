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

import com.salgulok.logEntry.dto.request.TemplateCreateRequest;
import com.salgulok.logEntry.dto.response.LogEntryCreateResponse;
import com.salgulok.logEntry.dto.response.TemplateCreateResponse;
import java.net.URI;

@RestController
@RequestMapping("/logs/{logId}/entries")
@RequiredArgsConstructor
public class LogEntryController {

    private final LogEntryService logEntryService;

    @PostMapping
    public ResponseEntity<LogEntryCreateResponse> createLogEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long logId,
            @RequestBody @Valid LogEntryCreateRequest request
    ) {
        LogEntryCreateResponse response = logEntryService.createLogEntry(user, logId, request);
        URI location = URI.create(String.format("/logs/%d/entries/%d", logId, response.getEntryId()));
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{entryId}/templates")
    public ResponseEntity<TemplateCreateResponse> addTemplateToEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long logId,
            @PathVariable Long entryId,
            @RequestBody @Valid TemplateCreateRequest request
    ) {
        TemplateCreateResponse response = logEntryService.addTemplateToEntry(user, logId, entryId, request);
        URI location = URI.create(String.format("/logs/%d/entries/%d/templates/%d", logId, entryId, response.getTemplateId()));
        return ResponseEntity.created(location).body(response);
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