package com.salgulok.logEntry.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "templates")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = PROTECTED)
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_entry_id", nullable = false)
    private LogEntry logEntry;

    @Column(nullable = false)
    private Long placeId;

    @Lob
    private String text;

    @Column(nullable = false)
    private int rating;

    @CreatedDate
    private LocalDateTime createdAt;

    public Template(LogEntry logEntry, Long placeId, String text, int rating) {
        this.logEntry = logEntry;
        this.placeId = placeId;
        this.text = text;
        this.rating = rating;
    }
}