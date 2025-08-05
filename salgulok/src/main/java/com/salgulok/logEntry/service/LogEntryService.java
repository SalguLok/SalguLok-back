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
}
