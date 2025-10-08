package com.salgulok.log.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.log.domain.Log;
import com.salgulok.log.dto.summary.LogScheduleDto;
import com.salgulok.log.repository.LogRepository;
import com.salgulok.region.domain.Region;
import com.salgulok.region.repository.RegionRepository;
import com.salgulok.user.domain.User;
import com.salgulok.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogScheduleService {
    private final UserService userService;
    private final RegionRepository regionRepository;
    private final LogRepository logRepository;
    private final RedisTemplate<String, LogScheduleDto> logScheduleRedisTemplate;
    private static final String LOG_START_PREFIX = "log:start:";
    private static final String LOG_END_PREFIX = "log:end:";

    public void registerTravelSchedule(Log log) {
        User user = userService.findByUserId(log.getUser().getUserId());
        Long userId = user.getUserId();

        LocalDate startDate = log.getStartDate();
        LocalDate endDate = log.getEndDate();
        LocalDate today = LocalDate.now();

        if (!startDate.isAfter(today)) {
            // 시작일이 오늘이거나 이미 지난 경우
            if (endDate.isAfter(today)) {
                user.updateTravelStatus(true, log.getRegion());
                // 종료 예약
                scheduleEndDateToRedis(userId, log, endDate);
            }
        } else {
            // 시작 & 종료 예약
            scheduleStartDateToRedis(userId, log, startDate);
            scheduleEndDateToRedis(userId, log, endDate);
        }
    }

    private void scheduleStartDateToRedis(Long userId, Log log, LocalDate startDate){
        LogScheduleDto startDto = new LogScheduleDto(userId, log.getLogId(), log.getRegion().getRegionId());
        String startKey = LOG_START_PREFIX + startDate;
        logScheduleRedisTemplate.opsForList().leftPush(startKey, startDto);
    }

    private void scheduleEndDateToRedis(Long userId, Log log, LocalDate endDate){
        LogScheduleDto endDto = new LogScheduleDto(userId, log.getLogId(), null);
        String endKey = LOG_END_PREFIX + endDate;
        logScheduleRedisTemplate.opsForList().leftPush(endKey, endDto);
    }

    public void applyStartScheduleAndCleanupRedis(Log log, LocalDate startDate){
        String key = LOG_START_PREFIX + startDate;
        try{
            List<LogScheduleDto> list = logScheduleRedisTemplate.opsForList().range(key, 0, -1);
            if(list == null || list.isEmpty()) return;

            for (LogScheduleDto dto : list) {
                if(dto.getLogId().equals(log.getLogId())) {
                    logScheduleRedisTemplate.opsForList().remove(key, 1, dto); // 1개 삭제
                }
            }
        } catch (Exception e){
            throw new SalgulokException(ErrorCode.REDIS_DELETE_ERROR);
        }
    }

    public void applyEndScheduleAndCleanupRedis(Log log, LocalDate endDate){
        String key = LOG_END_PREFIX + endDate;
        try{
            List<LogScheduleDto> list = logScheduleRedisTemplate.opsForList().range(key, 0, -1);
            if(list == null || list.isEmpty()) return;

            for (LogScheduleDto dto : list) {
                if(dto.getLogId().equals(log.getLogId())) {
                    logScheduleRedisTemplate.opsForList().remove(key, 1, dto); // 1개 삭제
                }
            }
        } catch (Exception e){
            throw new SalgulokException(ErrorCode.REDIS_DELETE_ERROR);
        }
    }

    @Async
    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정 실행
    @Transactional
    public void processStartSchedules() {
        LocalDate today = LocalDate.now();
        String key = LOG_START_PREFIX + today;

        List<LogScheduleDto> logSchedules = logScheduleRedisTemplate.opsForList().range(key, 0, -1);
        log.info("start log scheduling: {}", logSchedules.size());
        if (logSchedules == null || logSchedules.isEmpty()) return;

        for (LogScheduleDto dto : logSchedules) {
            try {
                Optional<Log> findLog = logRepository.findById(dto.getLogId());
                if(findLog.isEmpty()){
                    continue;
                }

                User user = userService.findByUserId(dto.getUserId());
                Region region = regionRepository.findById(dto.getRegionId())
                        .orElseThrow(() -> new SalgulokException(ErrorCode.REGION_NOT_FOUND));
                user.updateTravelStatus(true, region);
            } catch (Exception e) {
                log.error("Failed to process start schedule for userId={} logId={}", dto.getUserId(), dto.getLogId(), e);
            }
        }

        // Redis에서 삭제
        try {
            logScheduleRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete start schedule key: {}", key, e);
            throw new SalgulokException(ErrorCode.REDIS_DELETE_ERROR);
        }
    }

    @Async
    @Scheduled(cron = "0 59 23 * * *")  // 매일 23:59:0 실행
    @Transactional
    public void processEndSchedules() {
        LocalDate today = LocalDate.now();
        String key = LOG_END_PREFIX + today;
        log.info("end log scheduling: {}", key);

        List<LogScheduleDto> logSchedules = logScheduleRedisTemplate.opsForList().range(key, 0, -1);
        if (logSchedules == null || logSchedules.isEmpty()) return;

        for (LogScheduleDto dto : logSchedules) {
            try {
                Optional<Log> log = logRepository.findById(dto.getLogId());
                if(log.isEmpty()){
                    continue;
                }

                User user = userService.findByUserId(dto.getUserId());
                user.updateTravelStatus(false, null);
            } catch (Exception e) {
                log.error("Failed to process end schedule for userId={} logId={}", dto.getUserId(), dto.getLogId(), e);
            }
        }

        // Redis에서 삭제
        try {
            logScheduleRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete end schedule key: {}", key, e);
        }
    }

}
