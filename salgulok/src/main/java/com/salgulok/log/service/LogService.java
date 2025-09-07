package com.salgulok.log.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.log.domain.Log;
import com.salgulok.log.dto.request.LogCreateRequest;
import com.salgulok.log.dto.request.LogUpdateRequest;
import com.salgulok.log.dto.response.LogListResponse;
import com.salgulok.log.dto.summary.LogSummary;
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
    public LogSummary updateLog(User user, Long logId, LogUpdateRequest request) {
        Log log = findByLogId(logId);
        authorizeUser(user, log);

        Region updateRegion = findByRegionId(request.getRegionId());
        Log updateLog = log.updateLog(request, updateRegion);
        return LogSummary.from(updateLog);
    }

    @Transactional(readOnly = true)
    public LogListResponse getMyLog(User user) {
        List<Log> logs = logRepository.findByUserOrderByCreatedAtDesc(user);
        return new LogListResponse(logs.stream()
                .map(LogSummary::from)
                .collect(Collectors.toList()));
        //TODO: return하는 함수 중복. 리팩터링 필요
    }

    @Transactional(readOnly = true)
    public LogListResponse getLogByRegion(Long id) {
        Region region = findByRegionId(id);
        List<Log> logs = logRepository.findByRegion(region);
        return new LogListResponse(logs.stream()
                .map(LogSummary::from)
                .collect(Collectors.toList()));
    }

    // TODO: 페이징 필요
    @Transactional(readOnly = true)
    public LogListResponse getLogBySearch(String search) {
        List<Log> logs = logRepository.findByTitleContaining(search);
        return new LogListResponse(logs.stream()
                .map(LogSummary::from)
                .collect(Collectors.toList()));
    }

    private void authorizeUser(User user, Log log){
        if(!user.getUserId().equals(log.getUser().getUserId())){
            throw new SalgulokException(ErrorCode.OWNER_MISMATCH);
        }
    }

    private Log findByLogId(Long logId){
        return logRepository.findById(logId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));
    }

    private Region findByRegionId(Long regionId){
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.REGION_NOT_FOUND));
    }

    private void CheckIfValidDateRange(LocalDate startDate, LocalDate endDate){
        if(startDate.isAfter(endDate)){
            throw new SalgulokException(ErrorCode.INVALID_DATE_RANGE);
        }
    }
}
