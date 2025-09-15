package com.salgulok.log.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.log.domain.Log;
import com.salgulok.log.dto.request.LogCreateRequest;
import com.salgulok.log.dto.request.LogUpdateRequest;
import com.salgulok.log.dto.response.LogResponse;
import com.salgulok.log.repository.LogRepository;
import com.salgulok.region.domain.Region;
import com.salgulok.region.repository.RegionRepository;
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.List;

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
}