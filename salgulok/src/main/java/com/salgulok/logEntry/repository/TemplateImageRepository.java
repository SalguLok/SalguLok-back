package com.salgulok.logEntry.repository;

import com.salgulok.logEntry.domain.Template;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 장소별 템플릿(Template) 엔티티에 대한 JPA 레포지토리
 * - 하루 기록(LogEntry)에 포함된 장소 템플릿들을 저장/조회
 */
public interface TemplateRepository extends JpaRepository<Template, Long> {
}
