package com.salgulok.logEntry.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.log.domain.Log;
import com.salgulok.log.repository.LogRepository;
import com.salgulok.logEntry.domain.LogEntry;
import com.salgulok.logEntry.domain.Template;
import com.salgulok.logEntry.domain.TemplateImage;
import com.salgulok.logEntry.dto.request.LogEntryCreateRequest;
import com.salgulok.logEntry.dto.request.LogEntryUpdateRequest;
import com.salgulok.logEntry.dto.request.TemplateCreateRequest;
import com.salgulok.logEntry.dto.request.TemplateUpdateRequest;
import com.salgulok.logEntry.dto.response.LogEntryCreateResponse;
import com.salgulok.logEntry.dto.response.LogEntryUpdateResponse;
import com.salgulok.logEntry.repository.LogEntryRepository;
import com.salgulok.logEntry.repository.TemplateImageRepository;
import com.salgulok.logEntry.repository.TemplateRepository;
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    /** private final PlaceRepository placeRepository; */

    /**
     * com.salgulok.place.repository.PlaceRepository 만들거라는 가정! 으로 추가함
     */

    /**
     * 하루 기록 생성 (LogEntry + Templates + TemplateImages)
     *
     * @param user    현재 로그인한 사용자 (인증 정보)
     * @param request 하루 기록 저장 요청 DTO
     */
    @Transactional
    public LogEntryCreateResponse createLogEntry(User user, LogEntryCreateRequest request) {
        // 1. logId로 한 달 단위 살구록(Log) 조회
        Log log = logRepository.findById(request.getLogId())
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        // 2. 하루 기록(LogEntry) 생성 및 저장
        LogEntry logEntry = new LogEntry(log, request.getEntryDate());
        logEntryRepository.save(logEntry);

        // 3. 템플릿 리스트 생성
        List<Template> templates = request.getTemplates().stream()
                .map(templateReq -> new Template(
                        logEntry,
                        templateReq.getPlaceId(),
                        templateReq.getText(),
                        templateReq.getRating()
                ))
                .collect(Collectors.toList());

        // 4. 템플릿 일괄 저장
        List<Template> savedTemplates = templateRepository.saveAll(templates);

        // 5. 이미지 저장
        for (int i = 0; i < savedTemplates.size(); i++) {
            Template savedTemplate = savedTemplates.get(i);
            List<String> imageUrls = request.getTemplates().get(i).getImageUrls();

            if (imageUrls != null && !imageUrls.isEmpty()) {
                List<TemplateImage> images = imageUrls.stream()
                        .map(url -> new TemplateImage(savedTemplate, url))
                        .collect(Collectors.toList());
                templateImageRepository.saveAll(images);
            }
        }

        // 6. entryId + templateId + placeId 응답 리턴
        return LogEntryCreateResponse.from(logEntry, savedTemplates);
    }


    /**
     * 하루 기록 수정 (기존 템플릿 수정 + 이미지 수정 포함)
     */
    @Transactional
    public LogEntryUpdateResponse updateLogEntry(User user, Long entryId, LogEntryUpdateRequest request) {
        // 1. 로그 엔트리 조회
        LogEntry logEntry = logEntryRepository.findById(entryId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        // 2. 수정된 템플릿들을 담을 리스트
        List<Template> updatedTemplates = new ArrayList<>();

        // 3. 각 템플릿 수정 처리
        for (TemplateUpdateRequest templateReq : request.getTemplates()) {
            Template template = templateRepository.findById(templateReq.getTemplateId())
                    .orElseThrow(() -> new SalgulokException(ErrorCode.TEMPLATE_NOT_FOUND));

            // 텍스트, 별점 업데이트
            template.update(templateReq.getText(), templateReq.getRating());
            updatedTemplates.add(template);

            // 기존 이미지 삭제 후 새 이미지 저장
            templateImageRepository.deleteAllByTemplate(template);
            for (String imageUrl : templateReq.getImageUrls()) {
                templateImageRepository.save(new TemplateImage(template, imageUrl));
            }
        }

        // 4. 응답 리턴
        return LogEntryUpdateResponse.from(logEntry, updatedTemplates);
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

    /**
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
     */

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
