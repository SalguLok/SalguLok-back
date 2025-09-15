package com.salgulok.log.controller;

import com.salgulok.log.dto.request.LogCreateRequest;
import com.salgulok.log.dto.request.LogUpdateRequest;
import com.salgulok.log.dto.response.LogCreateResponse;
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

//    @PostMapping
//    public ResponseEntity<Void> createLog(@AuthenticationPrincipal User user,
//                                          @Valid @RequestBody LogCreateRequest request){
//        Long logId = logService.createLog(user, request);
//        return ResponseEntity.created(URI.create("/logs/"+logId)).build();
//    }

    @PostMapping

    public ResponseEntity<LogCreateResponse> createLog(@AuthenticationPrincipal User user,
                                                       @Valid @RequestBody LogCreateRequest request) {
        Long logId = logService.createLog(user, request);
        String location = "/logs/" + logId;
        return ResponseEntity
                .created(URI.create(location))
                .body(new LogCreateResponse(logId, location));
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

    //록 지역별 살구록
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

}


