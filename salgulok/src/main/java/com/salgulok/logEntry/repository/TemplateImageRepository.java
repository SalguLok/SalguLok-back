package com.salgulok.logEntry.repository;

import com.salgulok.logEntry.domain.Template;
import com.salgulok.logEntry.domain.TemplateImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 템플릿 이미지(TemplateImage) 엔티티에 대한 JPA 레포지토리
 * - 각 템플릿에 연결된 이미지들을 저장/조회/삭제
 */
public interface TemplateImageRepository extends JpaRepository<TemplateImage, Long> {


    List<TemplateImage> findAllByTemplate_TemplateId(Long templateId);

    // 해당 날짜(entry)의 이미지 중 가장 먼저 등록된 1장
    Optional<TemplateImage> findFirstByTemplate_LogEntry_LogEntryIdOrderByTemplateImageIdAsc(Long entryId);

    // 특정 템플릿에 속한 이미지 모두 삭제
    void deleteAllByTemplate(Template template);

}
