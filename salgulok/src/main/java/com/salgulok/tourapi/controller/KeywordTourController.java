package com.salgulok.tourapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.salgulok.tourapi.dto.LocationTourDto;
import com.salgulok.tourapi.service.KeywordTourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tour/keyword")

public class KeywordTourController {
    private final KeywordTourService keywordTourService;

    @GetMapping(value = "/search")
    public ResponseEntity<List<LocationTourDto>> searchPlain(@RequestParam String keyword) throws JsonProcessingException {
        return ResponseEntity.ok(keywordTourService.searchByKeyword(keyword));
    }

}
