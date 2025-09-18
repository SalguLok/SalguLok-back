package com.salgulok.logEntry.repository;

import com.salgulok.logEntry.domain.LogEntry;
import com.salgulok.logEntry.domain.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 장소별 템플릿(Template) 엔티티에 대한 JPA 레포지토리
 * - 하루 기록(LogEntry)에 포함된 장소 템플릿들을 저장/조회
 */

public interface TemplateRepository extends JpaRepository<Template, Long> {

    // 하루 기록에 포함된 모든 템플릿 조회
    List<Template> findAllByLogEntry(LogEntry logEntry);

    List<Template> findAllByLogEntry_LogEntryId(Long logEntryId);
    int countByLogEntry_LogEntryId(Long logEntryId);

    @Query("select avg(t.star) from Template t where t.placeId = :placeId")
    Double avgStarByPlaceId(@Param("placeId") Long placeId);

    int countByPlaceId(Long placeId);
}
