package com.salgulok.logEntry.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.log.domain.Log;
import com.salgulok.log.repository.LogRepository;
import com.salgulok.logEntry.domain.LogEntry;
import com.salgulok.logEntry.domain.Template;
import com.salgulok.logEntry.domain.TemplateImage;
import com.salgulok.logEntry.dto.request.*;
import com.salgulok.logEntry.dto.response.*;
import com.salgulok.logEntry.repository.LogEntryRepository;
import com.salgulok.logEntry.repository.TemplateImageRepository;
import com.salgulok.logEntry.repository.TemplateRepository;
import com.salgulok.places.domain.Place;
import com.salgulok.places.repository.PlaceRepository;
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.Collections;

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
    private final PlaceRepository placeRepository;

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
                        templateReq.getStar()
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
            template.update(templateReq.getText(), templateReq.getStar());
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

    @Transactional
    public PlaceRatingResponse savePlaceRating(PlaceRatingRequest request) {
        // 장소 조회
        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new SalgulokException(ErrorCode.PLACE_NOT_FOUND));

        Integer given=request.getStar();
        if(given==null||given<0||given>5){
            throw new SalgulokException(ErrorCode.INVALID_REQUEST);
        }

        double currentAvg=place.getStar()!=null?place.getStar():0.0;
        int currentCount=place.getStarCount()!=null?place.getStarCount():0;
        // 새 평점으로 평균 계산
        double totalScore = currentAvg*currentCount;
        int newCount = currentCount+1;
        double newAverage = (totalScore + given) / newCount;

        //소수점 첫째자리 반올림
        double roundedAvg= BigDecimal.valueOf(newAverage)
                .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();

        // 업데이트
        place.updateStar(newAverage, newCount);

        return new PlaceRatingResponse(roundedAvg,newCount);
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

    // === 조회: 특정 날짜 (엔트리 없으면 빈 응답) ===
    @Transactional(readOnly = true)
    public LogEntryDetailResponse getEntryByDateNullable(Long logId, LocalDate date) {
        return logEntryRepository.findByLog_LogIdAndEntryDate(logId, date)
                .map(entry -> {
                    List<Template> templates =
                            templateRepository.findAllByLogEntry_LogEntryId(entry.getLogEntryId());
                    return mapToDetail(entry, templates);
                })
                .orElseGet(() -> LogEntryDetailResponse.builder()
                        .logId(logId)
                        .entryDate(date)
                        .entryId(null)
                        .templateCount(0)
                        .templates(Collections.emptyList())
                        .build());
    }

    // === 조회: entryId 단건 상세 ===
    @Transactional(readOnly = true)
    public LogEntryDetailResponse getEntryDetail(Long entryId) {
        LogEntry entry = logEntryRepository.findById(entryId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        List<Template> templates =
                templateRepository.findAllByLogEntry_LogEntryId(entry.getLogEntryId());

        return mapToDetail(entry, templates);
    }

    // === 조회: 날짜 칩 리스트(날짜 + 대표 이미지 1장) ===
    @Transactional(readOnly = true)
    public LogEntryDateListResponse getEntryDatesWithThumbnail(Long logId) {
        List<LogEntry> entries = logEntryRepository.findAllByLog_LogIdOrderByEntryDateAsc(logId);

        List<LogEntryDateListResponse.Item> items = entries.stream().map(e -> {
            int tCount = templateRepository.countByLogEntry_LogEntryId(e.getLogEntryId());

            String thumbnail = templateImageRepository
                    .findFirstByTemplate_LogEntry_LogEntryIdOrderByTemplateImageIdAsc(e.getLogEntryId())
                    .map(TemplateImage::getImageUrl)
                    .orElse(null);

            return LogEntryDateListResponse.Item.builder()
                    .entryId(e.getLogEntryId())
                    .entryDate(e.getEntryDate())
                    .thumbnailUrl(thumbnail)
                    .templateCount(tCount)
                    .build();
        }).toList();

        return LogEntryDateListResponse.builder()
                .logId(logId)
                .items(items)
                .build();
    }

    // === 내부 매퍼: 상세 응답 변환 ===
    private LogEntryDetailResponse mapToDetail(LogEntry entry, List<Template> templates) {
        List<LogEntryDetailResponse.TemplateSummary> tSummaries = templates.stream().map(t -> {
            List<TemplateImage> imgs =
                    templateImageRepository.findAllByTemplate_TemplateId(t.getTemplateId());

            return LogEntryDetailResponse.TemplateSummary.builder()
                    .templateId(t.getTemplateId())
                    .placeId(t.getPlaceId())
                    .text(t.getText())
                    .rating(t.getStar())
                    .images(imgs.stream().map(i ->
                            LogEntryDetailResponse.ImageSummary.builder()
                                    .imageId(i.getTemplateImageId())
                                    .imageUrl(i.getImageUrl())
                                    .build()
                    ).toList())
                    .build();
        }).toList();

        return LogEntryDetailResponse.builder()
                .logId(entry.getLog().getLogId())
                .entryDate(entry.getEntryDate())
                .entryId(entry.getLogEntryId())
                .templateCount(tSummaries.size())
                .templates(tSummaries)
                .build();
    }

}
