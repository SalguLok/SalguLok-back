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
import com.salgulok.places.repository.PlaceRepository;
import com.salgulok.places.service.PlaceService;
import com.salgulok.user.domain.User;
import com.salgulok.image.repository.ImageMetaRepository;
import com.salgulok.image.domain.ImageMeta;
import com.salgulok.places.domain.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

import com.salgulok.logEntry.dto.response.DayFillState;
import com.salgulok.logEntry.dto.response.FillCalendarResponse;
import com.salgulok.image.infra.ImageUrlResolver;

@Service
@RequiredArgsConstructor
public class LogEntryService {

    private final LogRepository logRepository;
    private final LogEntryRepository logEntryRepository;
    private final TemplateRepository templateRepository;
    private final TemplateImageRepository templateImageRepository;
    private final ImageMetaRepository imageMetaRepository;
    private final PlaceService placeService;

    private final PlaceRepository placeRepository;
    private final ImageUrlResolver imageUrlResolver;

    @Transactional
    public Long createLogEntry(User user, Long logId, LogEntryCreateRequest request) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.SALGULOG_NOT_FOUND));

        // TODO: log.getUser()와 user 동일성 검증

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
            TemplateCreateRequest tReq = request.getTemplates().get(i);

            // 1) 신규 권장: imageIds 기반 attach
            attachTemplateImagesByIds(user, savedTemplate, tReq.getImageIds());

            // 2) 호환: 예전 images가 있으면 사용
            if ((tReq.getImageIds() == null || tReq.getImageIds().isEmpty())
                    && tReq.getImages() != null && !tReq.getImages().isEmpty()) {
                attachTemplateImagesLegacy(savedTemplate, tReq.getImages());
            }
        }

        // 저장한 템플릿들의 placeId 묶기
        var affectedPlaceIds = savedTemplates.stream()
                .map(Template::getPlaceId)
                .collect(Collectors.toSet());

        // 장소별 평점 재계산
        for (Long pid : affectedPlaceIds) {
            placeService.recalcAndUpdateRating(pid);
        }
        return logEntry.getLogEntryId();
    }


    @Transactional
    public void deleteTemplate(User user,Long logId,Long entryId,Long templateId){
        Template template=templateRepository.findById(templateId)
                .orElseThrow(()->new SalgulokException((ErrorCode.TEMPLATE_NOT_FOUND)));

        LogEntry templateEntry=template.getLogEntry();
        if(!Objects.equals(templateEntry.getLogEntryId(),entryId)){
            throw new SalgulokException(ErrorCode.LOG_ENTRY_NOT_IN_LOG);
        }

        validateLogEntryOwnership(logId,templateEntry);

        Long affectedPlaceId=template.getPlaceId();

        templateImageRepository.deleteAllByTemplate(template);

        templateRepository.delete(template);

        placeService.recalcAndUpdateRating(affectedPlaceId);
    }

    @Transactional
    public TemplateUpdateResponse updateSingleTemplate(
            User user, Long logId, Long entryId, Long templateId, TemplateSingleUpdateRequest req) {

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.TEMPLATE_NOT_FOUND));

        LogEntry templateEntry = template.getLogEntry();

        // entryId 일치 확인
        if (!Objects.equals(templateEntry.getLogEntryId(), entryId)) {
            throw new SalgulokException(ErrorCode.LOG_ENTRY_NOT_IN_LOG);
        }

        // logId 일치 확인
        validateLogEntryOwnership(logId, templateEntry);
        // TODO: user 권한 검사

        // ⭐ 부분 업데이트: null이면 기존값 유지해서 update(...) 호출
        String newText = (req.getText() != null) ? req.getText() : template.getText();
        Integer reqStar = req.getStar();
        int newStar = (reqStar != null) ? reqStar : template.getStar();

        // (선택) 별점 유효성 체크
        if (reqStar != null && (reqStar < 1 || reqStar > 5)) {
            throw new SalgulokException(ErrorCode.INVALID_REQUEST, "star must be between 1 and 5");
        }

        // 엔티티 메서드 그대로 사용 (Template는 변경 안 함)
        template.update(newText, newStar);

        // 이미지 교체 플래그: imageIds 또는 images가 존재하면 전체 교체
        boolean willReplaceImages = (req.getImageIds() != null) || (req.getImages() != null);
        if (willReplaceImages) {
            templateImageRepository.deleteAllByTemplate(template);

            if (req.getImageIds() != null) {
                attachTemplateImagesByIds(user, template, req.getImageIds());
            } else if (req.getImages() != null) {
                attachTemplateImagesLegacy(template, req.getImages());
            }
        }

        // 장소 평점 재계산
        placeService.recalcAndUpdateRating(template.getPlaceId());

        // 최신 이미지 목록으로 응답 구성
        List<TemplateImage> imgs =
                templateImageRepository.findAllByTemplate_TemplateId(template.getTemplateId());

        return TemplateUpdateResponse.of(template, imgs, imageUrlResolver);
    }


    @Transactional
    public LogEntryUpdateResponse updateLogEntry(User user, Long logId, Long entryId, LogEntryUpdateRequest request) {
        LogEntry logEntry = findLogEntryById(entryId);
        validateLogEntryOwnership(logId, logEntry);
        // TODO: user 권한 검사

        List<Template> updatedTemplates = new ArrayList<>();
        Set<Long> affectedPlaceIds = new HashSet<>();

        for (TemplateUpdateRequest templateReq : request.getTemplates()) {
            Template template = templateRepository.findById(templateReq.getTemplateId())
                    .orElseThrow(() -> new SalgulokException(ErrorCode.TEMPLATE_NOT_FOUND));

            template.update(templateReq.getText(), templateReq.getStar());
            updatedTemplates.add(template);
            affectedPlaceIds.add(template.getPlaceId());

            // 전체 교체
            templateImageRepository.deleteAllByTemplate(template);
            // (선택) templateImageRepository.deleteByTemplate_TemplateId(template.getTemplateId());

            // 1) 신규 권장: imageIds 기반 attach
            attachTemplateImagesByIds(user, template, templateReq.getImageIds());

            // 2) 호환: 예전 images가 있으면 사용
            if ((templateReq.getImageIds() == null || templateReq.getImageIds().isEmpty())
                    && templateReq.getImages() != null && !templateReq.getImages().isEmpty()) {
                attachTemplateImagesLegacy(template, templateReq.getImages());
            }
        }

        for (Long pid : affectedPlaceIds) {
            placeService.recalcAndUpdateRating(pid);
        }

        return LogEntryUpdateResponse.from(logEntry, updatedTemplates);
    }



    @Transactional
    public void deleteLogEntry(User user, Long logId, Long entryId) {
        LogEntry logEntry = findLogEntryById(entryId);
        validateLogEntryOwnership(logId, logEntry);
        // TODO: user 권한 검사

        List<Template> templates = templateRepository.findAllByLogEntry(logEntry);

        Set<Long> affectedPlaceIds = templates.stream()
                .map(Template::getPlaceId)
                .collect(Collectors.toSet());

        for (Template template : templates) {
            templateImageRepository.deleteAllByTemplate(template);
            templateRepository.delete(template);
        }

        logEntryRepository.delete(logEntry);

        for (Long pid : affectedPlaceIds) {
            placeService.recalcAndUpdateRating(pid);
        }
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

            String objectKey = templateImageRepository
                    .findFirstByTemplate_LogEntry_LogEntryIdOrderByTemplateImageIdAsc(e.getLogEntryId())
                    .map(TemplateImage::getObjectKey)
                    .orElse(null);

            return LogEntryDateListResponse.Item.builder()
                    .entryId(e.getLogEntryId())
                    .entryDate(e.getEntryDate())
                    .thumbnailObjectKey(objectKey)
                    .templateCount(tCount)
                    .build();
        }).toList();

        return LogEntryDateListResponse.builder()
                .logId(logId)
                .items(items)
                .build();
    }

    private LogEntryDetailResponse mapToDetail(LogEntry entry, List<Template> templates) {

//        var placeIdSet = templates.stream()
//                .map(Template::getPlaceId)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());
//
//        var idToName = placeRepository.findAllById(placeIdSet).stream()
//                .collect(Collectors.toMap(Place::getPlaceId,
//                        p -> p.getPlaceName() != null ? p.getPlaceName() : ""));
        // 1) 템플릿의 placeId 모으기
        Set<Long> placeIdSet = templates.stream()
                .map(Template::getPlaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2) 먼저 PK로 한 번에 조회
        Map<Long, String> idToName = placeRepository.findAllById(placeIdSet).stream()
                .collect(Collectors.toMap(
                        Place::getPlaceId,
                        p -> p.getPlaceName() != null ? p.getPlaceName() : ""
                ));

        // 3) 매칭 안 된 ID들에 대해서는 contentId로 재시도
        Set<Long> remaining = new HashSet<>(placeIdSet);
        remaining.removeAll(idToName.keySet()); // PK로 못 찾은 것들

        if (!remaining.isEmpty()) {
            List<Place> byContentIds = placeRepository.findByContentIdIn(remaining);
            for (Place p : byContentIds) {
                Long contentId = p.getContentId();
                if (contentId != null && remaining.contains(contentId)) {
                    idToName.put(contentId, p.getPlaceName() != null ? p.getPlaceName() : "");
                }
            }
        }

        List<LogEntryDetailResponse.TemplateSummary> tSummaries = templates.stream().map(t -> {
            List<TemplateImage> imgs =
                    templateImageRepository.findAllByTemplate_TemplateId(t.getTemplateId());

            String placeName = idToName.getOrDefault(t.getPlaceId(), "");

            return LogEntryDetailResponse.TemplateSummary.builder()
                    .templateId(t.getTemplateId())
                    .placeId(t.getPlaceId())
                    .placeName(placeName)
                    .text(t.getText())
                    .star(t.getStar())
                    .images(imgs.stream().map(i -> {
                        String resolvedUrl = imageUrlResolver.resolveUrlOrDefault(i.getImageUrl(), i.getObjectKey());
                        return LogEntryDetailResponse.ImageSummary.builder()
                                .imageId(i.getTemplateImageId())
                                .objectKey(i.getObjectKey())
                                .imageUrl(i.getImageUrl())
                                .resolvedUrl(resolvedUrl)
                                .build();
                    }).toList())
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

    private void attachTemplateImagesByIds(User user, Template template, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;

        for (Long imageId : imageIds) {
            ImageMeta meta = imageMetaRepository.findById(imageId)
                    .orElseThrow(() -> new SalgulokException(ErrorCode.IMAGE_NOT_FOUND, "imageId=" + imageId));

            // 소유자 검증
            if (!meta.getUser().getUserId().equals(user.getUserId())) {
                throw new SalgulokException(ErrorCode.OWNER_MISMATCH, "imageId=" + imageId);
            }

            // 중복 attach 방지
            boolean exists = templateImageRepository
                    .existsByTemplate_TemplateIdAndObjectKey(template.getTemplateId(), meta.getObjectKey());
            if (exists) continue;

            // objectKey 방어: 필수 값
            if (meta.getObjectKey() == null || meta.getObjectKey().isBlank()) {
                throw new SalgulokException(ErrorCode.INVALID_REQUEST, "image objectKey is required");
            }

            // URL resolver로 표준화된 URL 생성 (기존 URL이 있으면 사용, 없으면 objectKey로 생성)
            String resolvedUrl = imageUrlResolver.resolveUrlOrDefault(meta.getUrl(), meta.getObjectKey());

            TemplateImage ti = new TemplateImage(
                    template,
                    meta.getObjectKey(),
                    resolvedUrl,
                    meta.getFileName(),
                    meta.getContentType(),
                    meta.getSize()
            );
            templateImageRepository.save(ti);

            // 선택: 상태 전환 (원하면 사용)
            meta.markAttached(); // ImageMeta.Status.ATTACHED
            // JPA Dirty Checking으로 업데이트 반영
        }
    }

    private void attachTemplateImagesLegacy(Template template, List<TemplateCreateRequest.ImageRequest> images) {
        if (images == null || images.isEmpty()) return;

        List<TemplateImage> toSave = images.stream()
                .map(img -> new TemplateImage(
                        template,
                        img.getObjectKey(),
                        img.getUrl(),
                        img.getFileName(),
                        img.getContentType(),
                        img.getSize()
                ))
                .toList();

        templateImageRepository.saveAll(toSave);
    }

}