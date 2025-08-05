package com.salgulok.logEntry.domain;

import com.salgulok.log.domain.Log;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "log_entries")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = PROTECTED)
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logEntryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private Log log;

    @Column(nullable = false)
    private LocalDate entryDate;

    @CreatedDate
    private LocalDateTime createdAt;

    public LogEntry(Log log, LocalDate entryDate) {
        this.log = log;
        this.entryDate = entryDate;
    }

    @Lob
    private String summary; // 하루 마무리 멘트
}
