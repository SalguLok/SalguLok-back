package com.salgulok.tourapi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationTourDto {
    private String title;       // 관광지명
    private String address;     // 주
    private String tel;         // 전화번호
    private double mapX;        // 경도
    private double mapY;        // 위도
    private String contentType; // 콘텐츠 유형 (관광지/숙박/음식 등)
    private double distance;    // 거리 (m)
    // 추가
    private Long contentId;         // contentid
    private Integer contentTypeId;  // contenttypeid 원본
    private String imageUrl1;       // firstimage
    private String imageUrl2;       // firstimage2
    private String copyrightCode;   // cpyrhtDivCd
}
