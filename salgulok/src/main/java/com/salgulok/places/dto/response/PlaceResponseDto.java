package com.salgulok.places.dto.response;

import com.salgulok.places.domain.Place;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
                .star(place.getStar() != null ? place.getStar() : 0.0)
                .starCount(place.getStarCount()!=null?place.getStarCount():0)
                .build();
    }
}