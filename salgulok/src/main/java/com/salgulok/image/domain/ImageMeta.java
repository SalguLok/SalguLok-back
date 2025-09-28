package com.salgulok.image.domain;

import com.salgulok.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "image_meta",
        indexes = {
                @Index(name = "ux_image_meta_user_object_key", columnList = "user_id, object_key", unique = true)
        }
)
@EntityListeners(AuditingEntityListener.class)
public class ImageMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ← confirm 응답의 imageId

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    @Column(name = "url", length = 1024)
    private String url;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "size_bytes")
    private Long size;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private Status status; // CONFIRMED, ATTACHED

    @Column(name = "thumbnail_object_key", length = 512)
    private String thumbnailObjectKey;

    @Column(name = "thumbnail_url", length = 1024)
    private String thumbnailUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "thumbnail_status", length = 32)
    private ThumbnailStatus thumbnailStatus = ThumbnailStatus.EMPTY;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void markAttached() {
        this.status = Status.ATTACHED;
    }

    public void updateThumbnailInfo(String key, String url) {
        this.thumbnailObjectKey = key;
        this.thumbnailUrl = url;
        this.thumbnailStatus = ThumbnailStatus.DONE;
    }

    public void markThumbnailFailed() {
        this.thumbnailStatus = ThumbnailStatus.FAILED;
    }

    public void markThumbnailPending() {
        this.thumbnailStatus = ThumbnailStatus.PENDING;
    }

    public enum Status {CONFIRMED, ATTACHED}

    public enum ThumbnailStatus {EMPTY, PENDING, DONE, FAILED}
}
