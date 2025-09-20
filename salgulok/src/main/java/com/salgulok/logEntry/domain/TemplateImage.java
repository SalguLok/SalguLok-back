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
    private String objectKey;

    private String imageUrl;
    private String fileName;
    private String contentType;
    private Long imageSize;

    @CreatedDate
    private LocalDateTime createdAt;

    public TemplateImage(Template template, String objectKey, String imageUrl, String fileName, String contentType, Long imageSize) {
        this.template = template;
        this.objectKey = objectKey;
        this.imageUrl = imageUrl;
        this.fileName = fileName;
        this.contentType = contentType;
        this.imageSize = imageSize;
    }
}
