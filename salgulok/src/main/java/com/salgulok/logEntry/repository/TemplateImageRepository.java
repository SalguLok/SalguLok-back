package com.salgulok.logEntry.repository;

import com.salgulok.logEntry.domain.Template;
import com.salgulok.logEntry.domain.TemplateImage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 템플릿 이미지(TemplateImage) 엔티티에 대한 JPA 레포지토리
 * - 각 템플릿에 연결된 이미지들을 저장/조회/삭제
 */
public interface TemplateImageRepository extends JpaRepository<TemplateImage, Long> {

    // 특정 템플릿에 속한 이미지 모두 삭제
    void deleteAllByTemplate(Template template);
}
