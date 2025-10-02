package com.salgulok.places.dto.response;

import com.salgulok.places.domain.Place;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceResponseDto {
    private Long place_id;
    private String placeName;
    private double mapx;
    private double mapy;
    private int content_type_id;
    private String image_url;
    private String addr1;
    private String addr2;
    private String tel;
    private String overview;
    private double star;
    private int starCount;

    @Setter
    private Long logCount;

    public static PlaceResponseDto from(Place place) {
        return PlaceResponseDto.builder()
                .place_id(place.getPlaceId())
                .placeName(place.getPlaceName())
                .mapx(place.getMapx())
                .mapy(place.getMapy())
                .content_type_id(place.getContentTypeId())
                .image_url(place.getImageUrl())
                .addr1(place.getAddr1())
                .addr2(place.getAddr2())
                .tel(place.getTel())
                .overview(place.getOverview())
                .star(round2(place.getStar()))
                .starCount(place.getStarCount()!=null?place.getStarCount():0)
                .logCount(0L)
                .build();
    }

    public static PlaceResponseDto from(Place p, long logCount) {
        PlaceResponseDto dto = from(p);
        dto.setLogCount(logCount);
        return dto;
    }
    private static double round2(Double v) {
        if (v == null) return 0.0;
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}