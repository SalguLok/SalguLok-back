package com.salgulok.log.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.log.domain.Log;
import com.salgulok.log.dto.request.LogCreateRequest;
import com.salgulok.log.dto.request.LogUpdateRequest;
import com.salgulok.log.dto.response.LogListResponse;
import com.salgulok.log.dto.response.LogResponse;
import com.salgulok.log.repository.LogRepository;
import com.salgulok.region.domain.Region;
import com.salgulok.region.repository.RegionRepository;
import com.salgulok.user.domain.User;
import com.salgulok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {
    private final LogRepository logRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final ThreadPoolTaskScheduler taskScheduler;

    // logId 기준으로 start/end 스케줄 관리
    private final Map<Long, ScheduledFuture<?>> startTasks = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> endTasks = new ConcurrentHashMap<>();

    @Transactional
    public Long createLog(User user, LogCreateRequest request) {
        // 시작날짜가 종료날짜보다 이전인지 확인
        CheckIfValidDateRange(request.getStartDate(), request.getEndDate());

        Region region = findByRegionId(request.getRegionId());
        Log log = request.toEntity(user, region);
        Log saveLog = logRepository.save(log);

        // 체류중/체류전 변환 스케쥴링
        LocalDateTime startDateTime = log.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = log.getEndDate().plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        if(!startDateTime.isAfter(now) && endDateTime.isAfter(now)){
            // startDate가 오늘 또는 과거이고, endDate는 아직 안 지난 경우
            log.getUser().startTravel(log.getRegion());
            scheduleTravelEnd(log);
        } else if (startDateTime.isAfter(now)) {
            // 미래의 경우
            scheduleTravelStart(log);
            scheduleTravelEnd(log);
        }

        return saveLog.getLogId();
    }

    @Transactional
    public void deleteLog(User user, Long logId) {
        Log log = findByLogId(logId);
        authorizeUser(user, log);
        logRepository.delete(log);

        // 로그 삭제 시 예정된 체류전/중 전환 스케쥴링 취소
        cancelSchedules(log.getLogId());
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

    // Log 추가 시 startDate에 맞춰 실행 → 체류중으로 전환
    private void scheduleTravelStart(Log addLog) {
        cancelStartTask(addLog.getLogId()); // 기존 스케줄 취소 (중복 방지)

        Runnable task = () -> {
            try{
                User user = addLog.getUser();
                user.startTravel(addLog.getRegion());
                userRepository.save(user);
                log.info("add user info");
            } finally {
                startTasks.remove(addLog.getLogId());  // 스케쥴 작업 후 제거
            }
        };

//        Date startDate = Date.from(
//                log.getStartDate().atStartOfDay()
//                        .atZone(ZoneId.systemDefault())
//                        .toInstant()
//        );

        Date startDate = Date.from(addLog.getCreatedAt().plusMinutes(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        );

        log.info("StartDate: {}", startDate);

        ScheduledFuture<?> future = taskScheduler.schedule(task, startDate);
        startTasks.put(addLog.getLogId(), future);
    }

    // endDate 다음날 0시 실행 → 여행 종료 처리
    private void scheduleTravelEnd(Log log) {
        cancelEndTask(log.getLogId()); // 기존 스케줄 취소 (중복 방지)

        Runnable task = () -> {
            try{
                User user = log.getUser();
                user.endTravel();
                userRepository.save(user);
            } finally {
                endTasks.remove(log.getLogId());  // 스케쥴 작업 후 제거
            }
        };

        Date endDate = Date.from(
                log.getEndDate().plusDays(2).atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        ScheduledFuture<?> future = taskScheduler.schedule(task, endDate);
        endTasks.put(log.getLogId(), future);
    }

    // 로그 삭제 시 스케줄 취소
    private void cancelSchedules(Long logId) {
        cancelStartTask(logId);
        cancelEndTask(logId);

        log.info("log delete {}", logId);
    }

    // 로그 id로 start task 삭제
    private void cancelStartTask(Long logId) {
        ScheduledFuture<?> future = startTasks.remove(logId);
        if (future != null) future.cancel(false);
    }

    // 로그 id로 end task 삭제
    private void cancelEndTask(Long logId) {
        ScheduledFuture<?> future = endTasks.remove(logId);
        if (future != null) future.cancel(false);
    }

    // log 조회
    private Log findByLogId(Long logId) {
        return logRepository.findById(logId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));
    }

    // 지역 조회
    private Region findByRegionId(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.REGION_NOT_FOUND));
    }

    // 로그 등록 시 유효한 범위인지 확인
    private void CheckIfValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new SalgulokException(ErrorCode.INVALID_DATE_RANGE);
        }
    }
}

