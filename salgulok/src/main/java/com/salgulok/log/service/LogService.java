package com.salgulok.log.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.log.domain.Log;
import com.salgulok.log.dto.request.LogCheckRequest;
import com.salgulok.log.dto.request.LogCreateRequest;
import com.salgulok.log.dto.request.LogUpdateRequest;
import com.salgulok.log.dto.response.LogDateCheckResponse;
import com.salgulok.log.dto.response.LogListResponse;
import com.salgulok.log.dto.response.LogResponse;
import com.salgulok.log.repository.LogRepository;
import com.salgulok.region.domain.Region;
import com.salgulok.region.repository.RegionRepository;
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepository logRepository;
    private final RegionRepository regionRepository;

    @Transactional
    public Long createLog(User user, LogCreateRequest request) {
        // 시작날짜가 종료날짜보다 이전인지 확인
        CheckIfValidDateRange(request.getStartDate(), request.getEndDate());

        Region region = findByRegionId(request.getRegionId());
        Log log = request.toEntity(user, region);
        Log saveLog = logRepository.save(log);
        return saveLog.getLogId();
    }

    @Transactional
    public void deleteLog(User user, Long logId) {
        Log log = findByLogId(logId);
        authorizeUser(user, log);
        logRepository.delete(log);
    }

    @Transactional
    public LogResponse updateLog(User user, Long logId, LogUpdateRequest request) {
        Log log = findByLogId(logId);
        authorizeUser(user, log);

        Region updateRegion = findByRegionId(request.getRegionId());
        Log updateLog = log.updateLog(request, updateRegion);
        return LogResponse.from(updateLog);
    }

    @Transactional(readOnly = true)
    public LogListResponse getMyLog(User user) {
        List<Log> logs = logRepository.findByUserOrderByCreatedAtDesc(user);
        return new LogListResponse(logs.stream()
                .map(LogResponse::from)
                .collect(Collectors.toList()));
        //TODO: return하는 함수 중복. 리팩터링 필요
    }

    @Transactional(readOnly = true)
    public LogListResponse getLogByRegion(Long id) {
        Region region = findByRegionId(id);
        List<Log> logs = logRepository.findByRegionAndIsPublicTrue(region);
        return new LogListResponse(logs.stream()
                .map(LogResponse::from)
                .collect(Collectors.toList()));
    }

    // TODO: 페이징 필요
    @Transactional(readOnly = true)
    public LogListResponse getLogBySearch(String search) {
        List<Log> logs = logRepository.findByTitleContainingAndIsPublicTrue(search);
        return new LogListResponse(logs.stream()
                .map(LogResponse::from)
                .collect(Collectors.toList()));
    }


    private void authorizeUser(User user, Log log) {
        if (!user.getUserId().equals(log.getUser().getUserId())) {
            throw new SalgulokException(ErrorCode.OWNER_MISMATCH);
        }
    }

    private Log findByLogId(Long logId) {
        return logRepository.findById(logId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));
    }

    private Region findByRegionId(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.REGION_NOT_FOUND));
    }

    private void CheckIfValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new SalgulokException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    @Transactional(readOnly = true)
    public List<LogResponse> getPublicLogs() {
        return logRepository.findByIsPublicTrue()
                .stream().map(LogResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LogResponse> getLogsByUser(Long userId) {
        return logRepository.findByUser_UserId(userId)
                .stream().map(LogResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LogResponse getLogDetail(Long logId, User user) {
        Log log = findByLogId(logId);

        // 본인 로그가 아니고 비공개면 접근 제한
        if (!log.getIsPublic() && !log.getUser().getUserId().equals(user.getUserId())) {
            throw new SalgulokException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return LogResponse.from(log);
    }

    @Transactional
    public void increaseViewCount(Long logId, User user) {
        Log log = findByLogId(logId);
        // 본인 글은 조회수 증가 안함
        if (!log.getUser().getUserId().equals(user.getUserId())) {
            log.increaseView();
        }
    }

    @Transactional(readOnly = true)
    public LogDateCheckResponse checkDate(User user, LogCheckRequest request) {
        List<Log> overlappingLogs = logRepository.findOverlappingLogs(user.getUserId(), request.getStartDate(), request.getEndDate());
        if(!overlappingLogs.isEmpty()){
            return new LogDateCheckResponse(true);
        }
        return new LogDateCheckResponse(false);
    }

    @Transactional
    public void increaseLikeCount(Long logId, User user) {
        Log log = findByLogId(logId);
        // 본인이 누를 수 없는 제약
        if (!log.getUser().getUserId().equals(user.getUserId())) {
            log.increaseLikes();
        }
    }

    @Transactional
    public void decreaseLikeCount(Long logId, User user) {
        Log log = findByLogId(logId);
        if (!log.getUser().getUserId().equals(user.getUserId())) {
            log.decreaseLikes();
        }
    }

    @Transactional(readOnly = true)
    public List<LogResponse> getPopularLogs() {
        return logRepository.findByIsPublicTrueOrderByLikesDesc()
                .stream()
                .map(LogResponse::from)
                .toList();
    }

}
