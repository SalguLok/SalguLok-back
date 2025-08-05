package com.salgulok.logEntry.dto.request;

/**
 * 장소 평점 저장 요청 DTO
 */
public class PlaceRatingRequest {
    private Long placeId;
    private int rating;

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
