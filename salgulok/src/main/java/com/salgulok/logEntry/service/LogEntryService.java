package com.salgulok.logEntry.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.log.domain.Log;
import com.salgulok.log.repository.LogRepository;
import com.salgulok.logEntry.domain.LogEntry;
import com.salgulok.logEntry.domain.Template;
import com.salgulok.logEntry.domain.TemplateImage;
import com.salgulok.logEntry.dto.request.LogEntryCreateRequest;
import com.salgulok.logEntry.dto.request.TemplateCreateRequest;
import com.salgulok.logEntry.repository.LogEntryRepository;
import com.salgulok.logEntry.repository.TemplateImageRepository;
import com.salgulok.logEntry.repository.TemplateRepository;
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 하루 기록(LogEntry), 템플릿(Template), 이미지(TemplateImage)를 저장하는 서비스
 */
@Service
@RequiredArgsConstructor
public class LogEntryService {

    private final LogRepository logRepository;
    private final LogEntryRepository logEntryRepository;
    private final TemplateRepository templateRepository;
    private final TemplateImageRepository templateImageRepository;

    /**
     * 하루 기록 생성 (LogEntry + Templates + TemplateImages)
     *
     * @param user    현재 로그인한 사용자 (인증 정보)
     * @param request 하루 기록 저장 요청 DTO
     */
    @Transactional
    public void createLogEntry(User user, LogEntryCreateRequest request) {
        // 1. logId로 한 달 단위 살구록(Log) 조회
        Log log = logRepository.findById(request.getLogId())
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        // 2. 하루 기록(LogEntry) 생성 및 저장
        LogEntry logEntry = new LogEntry(log, request.getEntryDate());
        logEntryRepository.save(logEntry);

        // 3. 템플릿(Template) 리스트 반복 처리
        for (TemplateCreateRequest templateReq : request.getTemplates()) {
            // 템플릿 엔티티 생성
            Template template = new Template(
                    logEntry,
                    templateReq.getPlaceId(),
                    templateReq.getText(),
                    templateReq.getRating()
            );
            templateRepository.save(template);

            // 4. 이미지 URL 리스트 반복 저장
            if (templateReq.getImageUrls() != null) {
                for (String imageUrl : templateReq.getImageUrls()) {
                    TemplateImage image = new TemplateImage(template, imageUrl);
                    templateImageRepository.save(image);
                }
            }
        }
    }

    /**
     * 하루 기록 수정 (기존 템플릿 수정 + 이미지 수정 포함)
     */
    @Transactional
    public void updateLogEntry(User user, Long entryId, LogEntryUpdateRequest request) {
        LogEntry logEntry = logEntryRepository.findById(entryId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        for (TemplateUpdateRequest templateReq : request.getTemplates()) {
            Template template = templateRepository.findById(templateReq.getTemplateId())
                    .orElseThrow(() -> new SalgulokException(ErrorCode.TEMPLATE_NOT_FOUND));

            template.update(templateReq.getText(), templateReq.getRating());

            templateImageRepository.deleteAllByTemplate(template);

            for (String imageUrl : templateReq.getImageUrls()) {
                templateImageRepository.save(new TemplateImage(template, imageUrl));
            }
        }
    }

    /**
     * 하루 기록 삭제 (LogEntry + Templates + TemplateImages)
     */
    @Transactional
    public void deleteLogEntry(User user, Long entryId) {
        // 1. 삭제 대상 하루 기록 조회
        LogEntry logEntry = logEntryRepository.findById(entryId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        // 2. 해당 하루 기록에 속한 모든 템플릿 조회
        List<Template> templates = templateRepository.findAllByLogEntry(logEntry);

        for (Template template : templates) {
            // 2-1. 이미지 먼저 삭제
            templateImageRepository.deleteAllByTemplate(template);

            // 2-2. 템플릿 삭제
            templateRepository.delete(template);
        }

        // 3. 하루 기록(LogEntry) 삭제
        logEntryRepository.delete(logEntry);
    }

    /**
     * 템플릿 하나 삭제 (이미지 포함)
     */
    @Transactional
    public void deleteTemplate(Long templateId) {
        // 1. 삭제 대상 템플릿 조회
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.TEMPLATE_NOT_FOUND));

        // 2. 이미지 먼저 삭제
        templateImageRepository.deleteAllByTemplate(template);

        // 3. 템플릿 삭제
        templateRepository.delete(template);
    }

    @Transactional
    public void savePlaceRating(PlaceRatingRequest request) {
        // 장소 조회
        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new SalgulokException(ErrorCode.PLACE_NOT_FOUND));

        // 새 평점으로 평균 계산
        double totalScore = place.getRating() * place.getRatingCount();
        int newCount = place.getRatingCount() + 1;
        double newAverage = (totalScore + request.getRating()) / newCount;

        // 업데이트
        place.updateRating(newAverage, newCount);
    }

    @Transactional
    public void saveSummary(Long entryId, String summary) {
        LogEntry logEntry = logEntryRepository.findById(entryId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        logEntry.setSummary(summary);
    }

    @Transactional(readOnly = true)
    public String getSummary(Long entryId) {
        LogEntry logEntry = logEntryRepository.findById(entryId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        return logEntry.getSummary();
    }

}
