package com.salgulok.region.controller;

import com.salgulok.region.domain.Region;
import com.salgulok.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    // TourAPI에서 지역 코드 동기화 후 DB 저장
    @GetMapping("/regions/sync")
    public String syncRegions() {
        regionService.syncRegions();
        return "지역 코드 동기화 완료";
    }

    // DB에 저장된 모든 지역 정보 조회
    @GetMapping("/regions")
    public List<Region> getRegions() {
        return regionService.getAllRegions();
    }
}
