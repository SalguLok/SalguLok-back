package com.salgulok.tourapi.controller;

import com.salgulok.tourapi.dto.TourDetailDto;
import com.salgulok.tourapi.service.TourDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tour")
@RequiredArgsConstructor
public class TourDetailController {

    private final TourDetailService tourDetailService;

    /**
     * 관광지 상세 정보 조회 (detailCommon1 + detailIntro1 통합)
     *
     * @param contentId 콘텐츠 ID
     * @param contentTypeId 콘텐츠 타입 ID (선택사항, 없으면 introInfo는 null)
     * @return 통합된 상세 정보 JSON
     */
    @GetMapping("/detail")
    public ResponseEntity<TourDetailDto> getTourDetail(
            @RequestParam Long contentId,
            @RequestParam(required = false) Integer contentTypeId) {

        TourDetailDto detail = tourDetailService.getTourDetail(contentId, contentTypeId);
        return ResponseEntity.ok(detail);
    }
}