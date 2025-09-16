package com.salgulok.places.controller;

import com.salgulok.log.dto.response.LogResponse;
import com.salgulok.places.dto.response.PlaceResponseDto;
import com.salgulok.places.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    // 1. 키워드 지역 + 장소 검색
    @GetMapping
    public List<PlaceResponseDto> searchPlaces(@RequestParam("keyword") String keyword, @RequestParam(defaultValue="false") boolean sync) {
        if(sync){
            placeService.syncFromKeyword(keyword);
        }
        var results=placeService.searchPlaces(keyword);
        if(!sync&&results.isEmpty()){
            placeService.syncFromKeyword(keyword);
            results=placeService.searchPlaces(keyword);
        }

        return results;
    }

    // 2. 현재 위치 주변 장소 리스트
    @GetMapping("/map")
    public List<PlaceResponseDto> getPlacesAround(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false, defaultValue = "1000") int radius,
            @RequestParam(defaultValue="false") boolean sync
    ) {
        if(sync){
            placeService.syncFromTourApi(lat,lng,radius);
        }
        return placeService.getPlacesAround(lat, lng, radius);
    }

    // 3. 해당 장소 포함된 살구록 리스트 조회
    @GetMapping("/{placeId}/logs")
    public List<LogResponse> getLogsByPlace(@PathVariable Long placeId) {
        return placeService.getLogsByPlace(placeId);
    }


    // 4. 전체 인기장소
    @GetMapping("/popular")
    public List<PlaceResponseDto> getPopularPlaces() {
        return placeService.getPopularPlaces();
    }

    // 5. 지역별 인기 장소 조회
    @GetMapping("/popular/{regionId}")
    public List<PlaceResponseDto> getPopularPlacesByRegion(@PathVariable Long regionId) {
        return placeService.getPopularPlacesByRegion(regionId);
    }

    // 6. 장소 평점 평균 조회
    @GetMapping("/{placeId}/rating")
    public double getAverageRating(@PathVariable Long placeId) {
        return placeService.getAverageRating(placeId);
    }
}
