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
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.Collections;
import com.salgulok.logEntry.dto.response.DayFillState;
import com.salgulok.logEntry.dto.response.FillCalendarResponse;

@Service
@RequiredArgsConstructor
public class LogEntryService {

    private final LogRepository logRepository;
    private final LogEntryRepository logEntryRepository;
    private final TemplateRepository templateRepository;
    private final TemplateImageRepository templateImageRepository;

    @Transactional
    public Long createLogEntry(User user, Long logId, LogEntryCreateRequest request) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        // TODO: log.getUser()와 user가 동일한지 확인하는 로직 추가

        LogEntry logEntry = new LogEntry(log, request.getEntryDate());
        logEntryRepository.save(logEntry);

        List<Template> templates = request.getTemplates().stream()
                .map(templateReq -> new Template(
                        logEntry,
                        templateReq.getPlaceId(),
                        templateReq.getText(),
                        templateReq.getStar()
                ))
                .collect(Collectors.toList());

        List<Template> savedTemplates = templateRepository.saveAll(templates);

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
        return logEntry.getLogEntryId();
    }

    @Transactional
    public LogEntryUpdateResponse updateLogEntry(User user, Long logId, Long entryId, LogEntryUpdateRequest request) {
        LogEntry logEntry = findLogEntryById(entryId);
        validateLogEntryOwnership(logId, logEntry);
        // TODO: user 권한 검사

        List<Template> updatedTemplates = new ArrayList<>();

        for (TemplateUpdateRequest templateReq : request.getTemplates()) {
            Template template = templateRepository.findById(templateReq.getTemplateId())
                    .orElseThrow(() -> new SalgulokException(ErrorCode.TEMPLATE_NOT_FOUND));

            template.update(templateReq.getText(), templateReq.getStar());
            updatedTemplates.add(template);

            templateImageRepository.deleteAllByTemplate(template);
            for (String imageUrl : templateReq.getImageUrls()) {
                templateImageRepository.save(new TemplateImage(template, imageUrl));
            }
        }

        return LogEntryUpdateResponse.from(logEntry, updatedTemplates);
    }

    @Transactional
    public void deleteLogEntry(User user, Long logId, Long entryId) {
        LogEntry logEntry = findLogEntryById(entryId);
        validateLogEntryOwnership(logId, logEntry);
        // TODO: user 권한 검사

        List<Template> templates = templateRepository.findAllByLogEntry(logEntry);

        for (Template template : templates) {
            templateImageRepository.deleteAllByTemplate(template);
            templateRepository.delete(template);
        }

        logEntryRepository.delete(logEntry);
    }

    @Transactional
    public void saveSummary(Long logId, Long entryId, String summary) {
        LogEntry logEntry = findLogEntryById(entryId);
        validateLogEntryOwnership(logId, logEntry);
        logEntry.setSummary(summary);
    }

    @Transactional(readOnly = true)
    public String getSummary(Long logId, Long entryId) {
        LogEntry logEntry = findLogEntryById(entryId);
        validateLogEntryOwnership(logId, logEntry);
        return logEntry.getSummary();
    }

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

    @Transactional(readOnly = true)
    public LogEntryDetailResponse getEntryDetail(Long entryId) {
        LogEntry entry = findLogEntryById(entryId);
        List<Template> templates =
                templateRepository.findAllByLogEntry_LogEntryId(entry.getLogEntryId());
        return mapToDetail(entry, templates);
    }

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

    private LogEntryDetailResponse mapToDetail(LogEntry entry, List<Template> templates) {
        List<LogEntryDetailResponse.TemplateSummary> tSummaries = templates.stream().map(t -> {
            List<TemplateImage> imgs =
                    templateImageRepository.findAllByTemplate_TemplateId(t.getTemplateId());

            return LogEntryDetailResponse.TemplateSummary.builder()
                    .templateId(t.getTemplateId())
                    .placeId(t.getPlaceId())
                    .text(t.getText())
                    .star(t.getStar())
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

    @Transactional(readOnly = true)
    public FillCalendarResponse getFillCalendar(Long logId, LocalDate start, LocalDate end) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        LocalDate s = (start != null) ? start : log.getStartDate();
        LocalDate e = (end   != null) ? end   : log.getEndDate();

        if (s == null || e == null || s.isAfter(e)) {
            throw new SalgulokException(ErrorCode.INVALID_DATE_RANGE);
        }

        List<LogEntry> entries = logEntryRepository.findAllByLog_LogIdAndEntryDateBetween(logId, s, e);
        var dateToEntryId = entries.stream()
                .collect(Collectors.toMap(LogEntry::getEntryDate, LogEntry::getLogEntryId, (a, b) -> a));

        List<DayFillState> days = new ArrayList<>();
        for (LocalDate d = s; !d.isAfter(e); d = d.plusDays(1)) {
            Long entryId = dateToEntryId.get(d);
            boolean hasTemplate = false;
            if (entryId != null) {
                int cnt = templateRepository.countByLogEntry_LogEntryId(entryId);
                hasTemplate = cnt > 0;
            }
            days.add(new DayFillState(d, hasTemplate));
        }

        return new FillCalendarResponse(logId, s, e, days);
    }

    private LogEntry findLogEntryById(Long entryId) {
        return logEntryRepository.findById(entryId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));
    }

    private void validateLogEntryOwnership(Long logId, LogEntry logEntry) {
        if (!Objects.equals(logEntry.getLog().getLogId(), logId)) {
            throw new SalgulokException(ErrorCode.LOG_ENTRY_NOT_IN_LOG);
        }
    }
}