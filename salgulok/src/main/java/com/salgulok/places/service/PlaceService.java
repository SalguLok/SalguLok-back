package com.salgulok.places.service;

import com.salgulok.places.domain.Place;
import com.salgulok.places.dto.response.PlaceResponseDto;
import com.salgulok.places.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;

    public List<PlaceResponseDto> searchPlaces(String keyword) {
        List<Place> places = placeRepository.findByPlaceNameContaining(keyword);

        // 검색 결과가 없을 경우 외부 API 요청해서 가져오기

        return places.stream()
                .map(PlaceResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PlaceResponseDto> getPlacesAround(double lat, double lng, int radius) {
        List<Place> allPlaces = placeRepository.findAll();

        // 위도, 경도 기준 거리 계산
        return allPlaces.stream()
                .filter(place -> {
                    if (place.getMapx() == null || place.getMapy() == null) return false;
                    double distance = haversine(lat, lng, place.getMapy(), place.getMapx());
                    return distance <= radius;
                })
                .map(PlaceResponseDto::from)
                .collect(Collectors.toList());
    }

    // Haversine 공식: 두 좌표 간 거리
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 지구 반지름 (m)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<PlaceResponseDto> getPopularPlaces() {
        List<Place> popularPlaces = placeRepository.findPopularPlaces();

        return popularPlaces.stream()
                .map(PlaceResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PlaceResponseDto> getPopularPlacesByRegion(Long regionId) {
        // regionId는 문자열 주소(addr1)에 매핑됨 (ex. 제주도 → "제주특별자치도")
        String regionKeyword = convertRegionIdToKeyword(regionId);  // 아래 함수 참고

        List<Place> places = placeRepository.findPopularPlacesByRegion(regionKeyword);

        return places.stream()
                .map(PlaceResponseDto::from)
                .collect(Collectors.toList());
    }

    // 지역 ID → 문자열 주소 매핑
    private String convertRegionIdToKeyword(Long regionId) {
        return switch (regionId.intValue()) {
            case 1 -> "서울특별시";
            case 2 -> "부산광역시";
            case 3 -> "제주특별자치도";
            case 4 -> "경기도";
            default -> "";
        };
    }

    public double getAverageRating(Long placeId) {
        Double rating = placeRepository.findAverageRatingByPlaceId(placeId);
        return rating != null ? rating : 0.0;
    }
}
