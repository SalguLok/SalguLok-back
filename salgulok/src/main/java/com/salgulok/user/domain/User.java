package com.salgulok.user.domain;

import com.salgulok.region.domain.Region;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private Long kakaoId;

    @Column(unique = true)
    private String username;

    @Column(name = "profile_img")
    private String profileImg;

    private String intro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regionId")
    private Region region;  // 체류 지역

    @Column(name = "is_traveling", nullable = false)
    private Boolean isTraveling = false;    // 기본값

    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Builder
    public User(Long kakaoId) {
        this.kakaoId =  kakaoId;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateUserInfo(String username, String intro, String profileImg){
        this.username = username;
        this.intro = intro;
        this.profileImg = profileImg;
    }

    public void updateTravelStatus(Boolean isTraveling, Region region){
        this.isTraveling = isTraveling;
        this.region = region;
    }
}