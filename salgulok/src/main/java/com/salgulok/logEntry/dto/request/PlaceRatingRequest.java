package com.salgulok.logEntry.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 장소 평점 저장 요청 DTO
 */
public class PlaceRatingRequest {
    private Long placeId;

    @Min(0) @Max(5) private int star;

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }
}
