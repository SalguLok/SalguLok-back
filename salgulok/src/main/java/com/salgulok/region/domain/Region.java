package com.salgulok.region.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "regions")
@NoArgsConstructor(access = PROTECTED)
public class Region {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long regionId;

    @Column(nullable = false)
    private String name;

    public Region(long regionId, String name) {
        this.regionId = regionId;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}
