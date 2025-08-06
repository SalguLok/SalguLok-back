package com.salgulok.places.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long placeId;
    private String placeName;
    private Double mapx;
    private Double mapy;
    private Integer contentTypeId;
    private String imageUrl;
    private String addr1;
    private String addr2;
    private String tel;
    private String overview;
    private Double star;
}