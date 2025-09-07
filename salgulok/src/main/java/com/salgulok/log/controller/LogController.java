package com.salgulok.log.controller;

import com.salgulok.log.dto.request.LogCreateRequest;
import com.salgulok.log.dto.request.LogUpdateRequest;
import com.salgulok.log.dto.summary.LogSummary;
import com.salgulok.log.service.LogService;
import com.salgulok.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {
    private final LogService logService;

    @PostMapping
    public ResponseEntity<Void> createLog(@AuthenticationPrincipal User user,
                                          @Valid @RequestBody LogCreateRequest request){
        Long logId = logService.createLog(user, request);
        return ResponseEntity.created(URI.create("/logs/"+logId)).build();
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(@AuthenticationPrincipal User user,
                                          @PathVariable Long logId){
        logService.deleteLog(user, logId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{logId}")
    public ResponseEntity<LogSummary> updateLog(@AuthenticationPrincipal User user,
                                                @PathVariable Long logId,
                                                @Valid @RequestBody LogUpdateRequest request){
        LogSummary response = logService.updateLog(user, logId, request);
        return ResponseEntity.ok(response);
    }
}
