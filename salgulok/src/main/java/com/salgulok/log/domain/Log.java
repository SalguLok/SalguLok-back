package com.salgulok.log.domain;

import com.salgulok.log.dto.request.LogUpdateRequest;
import com.salgulok.logEntry.domain.LogEntry;
import com.salgulok.region.domain.Region;
import com.salgulok.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "logs")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = PROTECTED)
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regionId", nullable = false)
    private Region region;

    @OneToMany(mappedBy = "log", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<LogEntry> logEntries = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column
    private String imgUrl;

    @Lob
    @Column
    private String oneReview;

    @Column(nullable = false)
    private Long view = 0L;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false)
    private Long likes = 0L;

    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isUpload = false;

    @Builder
    public Log(User user, String title, LocalDate startDate, LocalDate endDate, boolean isPublic, Region region, String imgUrl, String oneReview) {
        this.user = user;
        this.region = region;
        this.title = title;
        this.imgUrl = imgUrl;
        this.oneReview = oneReview;
        this.view = 0L;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isPublic = isPublic;
    }

    public Log updateLog(LogUpdateRequest request, Region region) {
        this.title = request.getTitle();
        this.isPublic = request.getIsPublic();
        this.region = region;
        this.imgUrl = request.getImgUrl();
        this.oneReview = request.getOneReview();
        return this;
    }

    public void increaseView() {
        this.view += 1;
    }

    public void increaseLikes() {
        this.likes += 1;
    }

    public void decreaseLikes() {
        if (this.likes > 0) {
            this.likes -= 1;
        }
    }
    // 추가: 업로드(노출) 상태 토글
    public void setUpload(boolean upload) {
        this.isUpload = upload;
    }

}