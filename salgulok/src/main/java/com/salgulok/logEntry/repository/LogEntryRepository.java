package com.salgulok.logEntry.repository;

import com.salgulok.logEntry.domain.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 하루 단위 기록(LogEntry) 엔티티에 대한 JPA 레포지토리
 * - 기본적인 CRUD 메서드 제공
 */
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
}
