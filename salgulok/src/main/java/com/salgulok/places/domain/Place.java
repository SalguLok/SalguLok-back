package com.salgulok.places.domain;

import jakarta.persistence.*;

import lombok.*;


@Entity
@Table(
        name="places",
        uniqueConstraints=@UniqueConstraint(name="uk_places_content_id", columnNames="content_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long placeId;

    @Column(name="content_id")
    private Long contentId;

    private String placeName;
    private Double mapx;
    private Double mapy;
    private Integer contentTypeId;
    private String imageUrl;
    private String addr1;
    private String addr2;
    private String tel;
    private String overview;

    @Column(name="star", nullable=false)
    private Double star =0.0;

    @Column(name="star_count", nullable=false)
    private Integer starCount=0;

    @Column(name = "region_id")
    private Long regionId;

    public void updateStar(double newAverage, int newCount) {
        this.star = newAverage;
        this.starCount = newCount;
    }
}