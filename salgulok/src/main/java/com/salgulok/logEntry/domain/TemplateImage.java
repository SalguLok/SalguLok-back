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
@Table(name = "template_images")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = PROTECTED)
public class TemplateImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(nullable = false)
    private String imageUrl;

    @CreatedDate
    private LocalDateTime createdAt;

    public TemplateImage(Template template, String imageUrl) {
        this.template = template;
        this.imageUrl = imageUrl;
    }
}
