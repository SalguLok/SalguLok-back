package com.salgulok.tourapi.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.salgulok.tourapi.dto.LocationTourDto;
import com.salgulok.tourapi.service.LocationTourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tour")
public class LocationTourController {
    private final LocationTourService tourService;

    @GetMapping("/nearby")
    public ResponseEntity<List<LocationTourDto>> getNearbyTour(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "2000") int radius// 기본 반경 2km
    ) throws JsonProcessingException {
        List<LocationTourDto> results = tourService.getNearbyTourInfo(lat, lng, radius);
        return ResponseEntity.ok(results);
    }
}
