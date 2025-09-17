package com.salgulok.log.controller;

import com.salgulok.log.dto.request.LogCheckRequest;
import com.salgulok.log.dto.request.LogCreateRequest;
import com.salgulok.log.dto.request.LogUpdateRequest;
import com.salgulok.log.dto.response.LogCreateResponse;
import com.salgulok.log.dto.response.LogDateCheckResponse;
import com.salgulok.log.dto.response.LogListResponse;
import com.salgulok.log.dto.response.LogResponse;
import com.salgulok.log.service.LogService;
import com.salgulok.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {
    private final LogService logService;

    @PostMapping
    public ResponseEntity<LogCreateResponse> createLog(@AuthenticationPrincipal User user,
                                                       @Valid @RequestBody LogCreateRequest request) {
        Long logId = logService.createLog(user, request);
        String location = "/logs/" + logId;
        return ResponseEntity
                .created(URI.create(location))
                .body(new LogCreateResponse(logId, location));
    }

    @PostMapping("/checkDate")
    public ResponseEntity<LogDateCheckResponse> checkDate(@AuthenticationPrincipal User user,
                                                          @Valid @RequestBody LogCheckRequest request){
        LogDateCheckResponse response = logService.checkDate(user, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(@AuthenticationPrincipal User user,
                                          @PathVariable Long logId){
        logService.deleteLog(user, logId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{logId}")
    public ResponseEntity<LogResponse> updateLog(@AuthenticationPrincipal User user,
                                                 @PathVariable Long logId,
                                                 @Valid @RequestBody LogUpdateRequest request){
        LogResponse response = logService.updateLog(user, logId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<LogListResponse> getMyLog(@AuthenticationPrincipal User user){
        LogListResponse response = logService.getMyLog(user);
        return ResponseEntity.ok(response);
    }

    // 지역별 살구록
    @GetMapping("/region")
    public ResponseEntity<LogListResponse> getLogByRegion(@RequestParam("id") Long id){
        LogListResponse response = logService.getLogByRegion(id);
        return ResponseEntity.ok(response);
    }

    // 살구록 검색
    @GetMapping
    public ResponseEntity<LogListResponse> getLogBySearch(@RequestParam("search") String search){
        LogListResponse response = logService.getLogBySearch(search);
        return ResponseEntity.ok(response);
    }

    // 전체 공개 살구록 리스트
    @GetMapping("/public")
    public ResponseEntity<List<LogResponse>> getPublicLogs() {
        return ResponseEntity.ok(logService.getPublicLogs());
    }

    // 내 살구록 리스트
    @GetMapping("/me")
    public ResponseEntity<List<LogResponse>> getMyLogs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(logService.getLogsByUser(user.getUserId()));
    }

    // 특정 유저 살구록 리스트
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<LogResponse>> getUserLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(logService.getLogsByUser(userId));
    }

    // 특정 살구록 상세 조회 : 제목, 한줄평, 여행 시작일 및 종료일, 공개 여부, 여행 지역 ID, 대표 이미지 URL 모두 포함
    @GetMapping("/{logId}")
    public ResponseEntity<LogResponse> getLogDetail(@AuthenticationPrincipal User user,
                                                    @PathVariable Long logId) {
        return ResponseEntity.ok(logService.getLogDetail(logId, user));
    }

    // 조회수 증가
    @PostMapping("/{logId}/views")
    public ResponseEntity<Void> increaseView(@AuthenticationPrincipal User user,
                                             @PathVariable Long logId) {
        logService.increaseViewCount(logId, user);
        return ResponseEntity.ok().build();
    }
  
    // 좋아요 로직
    @PostMapping("/{logId}/likes")
    public ResponseEntity<Void> increaseLike(@AuthenticationPrincipal User user,
                                             @PathVariable Long logId) {
        logService.increaseLikeCount(logId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{logId}/likes")
    public ResponseEntity<Void> decreaseLike(@AuthenticationPrincipal User user,
                                             @PathVariable Long logId) {
        logService.decreaseLikeCount(logId, user);
        return ResponseEntity.ok().build();
    }

    // 로그 인기순 정렬 (프론트 실수 방지를 위해 공개 로그만 정렬함)
    @GetMapping("/popular")
    public ResponseEntity<List<LogResponse>> getPopularLogs() {
        return ResponseEntity.ok(logService.getPopularLogs());
    }


}
