package com.salgulok.auth.domain;

import com.salgulok.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = PROTECTED)
public class RefreshToken {

    @Id
    private Long userId;

    @Column(nullable = false, length = 500)
    private String token;

    private LocalDateTime expiryDate;

    @Builder
    public RefreshToken(User user, String token, LocalDateTime expiryDate) {
        this.userId = user.getUserId();
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public void updateToken(String token, LocalDateTime expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }
}

