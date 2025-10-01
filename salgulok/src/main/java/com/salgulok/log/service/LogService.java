package com.salgulok.log.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.log.domain.Log;
import com.salgulok.log.domain.LogComment;
import com.salgulok.log.domain.LogLike;

import com.salgulok.log.dto.request.*;
import com.salgulok.log.dto.response.*;
import com.salgulok.log.repository.LogCommentRepository;
import com.salgulok.log.repository.LogLikeRepository;
import com.salgulok.log.repository.LogRepository;

import com.salgulok.region.domain.Region;
import com.salgulok.region.repository.RegionRepository;
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepository logRepository;
    private final LogLikeRepository logLikeRepository;
    private final LogCommentRepository logCommentRepository;
    private final RegionRepository regionRepository;
    private static final int LogPage_paging_size = 4; // 추후 수정예정~ 일단 테스트로 4개만

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
        boolean isLiked = logLikeRepository.existsByUserAndLog(user, log);
        return LogResponse.from(updateLog, isLiked);
    }

    @Transactional(readOnly = true)
    public LogPagingListResponse getMyLog(User user, int page) {
        Pageable pageable = PageRequest.of(page, LogPage_paging_size);
        Page<Log> logs = logRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return new LogPagingListResponse(logs.map(log -> LogResponse.from(log, logLikeRepository.existsByUserAndLog(user, log))));
    }

    @Transactional(readOnly = true)
    public LogListResponse getLogByRegion(Long id) {
        Region region = findByRegionId(id);
        List<Log> logs = logRepository.findByRegionAndIsPublicTrueAndIsUploadTrue(region);
        return new LogListResponse(logs.stream()
                .map(log -> LogResponse.from(log, false)) // 로그인 안한 유저이므로 false
                .collect(Collectors.toList()));
    }

    // 살구록 검색 (키워드 검색/소팅/지역검색)
    @Transactional(readOnly = true)
    public LogPagingListResponse getLogBySearchAndFiltering(String search, String sort, Long regionId, int page, User user) {
        Sort sortOption;

        // 최신순, 조회순, 좋아요순 소팅
        switch (sort) {
            case "view":
                sortOption = Sort.by(Sort.Direction.DESC, "view");
                break;
            case "like":
                sortOption = Sort.by(Sort.Direction.DESC, "likes");
                break;
            default: // 기본값 최신순
                sortOption = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Pageable pageable = PageRequest.of(page, LogPage_paging_size, sortOption);  //sorting까지 추가해서 pageable 추가

        Page<Log> logs;

        if (regionId == 0) {
            // 지역 없는 경우 검색값으로 필터링
            if (search != null && !search.trim().isEmpty()) {
                logs = logRepository.findByTitleContainingAndIsPublicTrueAndIsUploadTrue(search, pageable);
            } else {    // 지역 코드 있는데 검색어 없는 경우
                logs = logRepository.findByIsPublicTrueAndIsUploadTrue(pageable);
            }
        } else {
            // 지역 필터링
            Region region = findByRegionId(regionId);
            // 검색어 없는 경우
            if (search != null && !search.trim().isEmpty()) {
                logs = logRepository.findByTitleContainingAndRegionAndIsPublicTrueAndIsUploadTrue(search, region, pageable);
            } else {    // 검색어 있는 경우
                logs = logRepository.findByRegionAndIsPublicTrueAndIsUploadTrue(region, pageable);
            }
        }

        return new LogPagingListResponse(logs.map(log -> {
            boolean isLiked = user != null && logLikeRepository.existsByUserAndLog(user, log);
            return LogResponse.from(log, isLiked);
        }));
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
    public List<LogResponse> getLogsByUser(Long userId, User currentUser) {
        return logRepository.findByUser_UserId(userId)
                .stream().map(log -> {
                    boolean isLiked = currentUser != null && logLikeRepository.existsByUserAndLog(currentUser, log);
                    return LogResponse.from(log, isLiked);
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LogResponse getLogDetail(Long logId, User user) {
        Log log = findByLogId(logId);

        // 본인 로그가 아니고 비공개면 접근 제한
        if (!log.getIsPublic() && (user == null || !log.getUser().getUserId().equals(user.getUserId()))) {
            throw new SalgulokException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        boolean isLiked = user != null && logLikeRepository.existsByUserAndLog(user, log);
        return LogResponse.from(log, isLiked);
    }

    @Transactional
    public void increaseViewCount(Long logId, User user) {
        Log log = findByLogId(logId);
        // 본인 글은 조회수 증가 안함
        if (user != null && !log.getUser().getUserId().equals(user.getUserId())) {
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

    // 좋아요 수 반환
    @Transactional(readOnly = true)
    public Long getLikeCount(Long logId) {
        Log log = findByLogId(logId);
        return log.getLikes(); // Long 그대로 반환
    }

    @Transactional
    public void increaseLikeCount(Long logId, User user) {
        Log log = findByLogId(logId);
        if (logLikeRepository.existsByUserAndLog(user, log)) {
            throw new SalgulokException(ErrorCode.ALREADY_LIKED_LOG);
        }

        LogLike logLike = new LogLike(user, log);
        logLikeRepository.save(logLike);
        log.increaseLikes();
    }

    @Transactional
    public void decreaseLikeCount(Long logId, User user) {
        Log log = findByLogId(logId);
        LogLike logLike = logLikeRepository.findByUserAndLog(user, log)
                .orElseThrow(() -> new SalgulokException(ErrorCode.LIKE_NOT_FOUND));

        logLikeRepository.delete(logLike);
        log.decreaseLikes();
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(User user, Long logId) {
        if (user == null) {
            return false;
        }
        Log log = findByLogId(logId);
        return logLikeRepository.existsByUserAndLog(user, log);
    }

    @Transactional
    public void updateUploadStatus(User user, Long logId, Boolean isUpload) {
        Log log = findByLogId(logId);
        authorizeUser(user, log);

        // 비공개 글은 업로드(게시) 불가
        if (Boolean.TRUE.equals(isUpload) && !Boolean.TRUE.equals(log.getIsPublic())) {
            throw new SalgulokException(ErrorCode.UNAUTHORIZED_ACCESS); // 필요하면 전용 에러코드 추가
        }

        log.setUpload(isUpload); // Log 엔티티에 setter 또는 전용 메서드 존재 가정 (예: log.updateUpload(isUpload))
    }

    @Transactional(readOnly = true)
    public List<LogResponse> getPublicLogs(User user) {
        return logRepository.findByIsPublicTrueAndIsUploadTrueOrderByCreatedAtDesc()
                .stream().map(log -> {
                    boolean isLiked = user != null && logLikeRepository.existsByUserAndLog(user, log);
                    return LogResponse.from(log, isLiked);
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<LogResponse> getPopularLogs(User user) {
        // ⚠ 기존 중복 메서드 제거: 이 메서드 단 하나만 유지
        return logRepository.findPopularPublicAndUploaded()
                .stream().map(log -> {
                    boolean isLiked = user != null && logLikeRepository.existsByUserAndLog(user, log);
                    return LogResponse.from(log, isLiked);
                }).toList();
    }

    @Transactional
    public Long createComment(User user, Long logId, LogCommentCreateRequest request) {
        Log log = findByLogId(logId);

        if (!log.getIsPublic()) {
            throw new SalgulokException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        LogComment comment = new LogComment(log, user, request.getContent());
        LogComment savedComment = logCommentRepository.save(comment);
        return savedComment.getId();
    }

    @Transactional(readOnly = true)
    public Page<LogCommentResponse> getComments(Long logId, Pageable pageable) {
        Log log = findByLogId(logId);

        return logCommentRepository.findByLogLogIdOrderByCreatedAtDesc(logId, pageable)
                .map(LogCommentResponse::new);
    }

    @Transactional
    public void deleteComment(User user, Long logId, Long commentId) {
        Log log = findByLogId(logId);
        LogComment comment = logCommentRepository.findById(commentId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        if (!comment.getLog().getLogId().equals(logId)) {
            throw new SalgulokException(ErrorCode.INVALID_REQUEST);
        }

        if (!comment.getAuthor().getUserId().equals(user.getUserId())) {
            throw new SalgulokException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        logCommentRepository.delete(comment);
    }

    // 한 줄 평 업로드
    @Transactional
    public void updateReview(User user, Long logId, LogReviewUpdateRequest request) {
        Log log = findByLogId(logId);
        authorizeUser(user, log);
        log.setOneReview(request.getOneReview()); // null이면 비우기
    }


}