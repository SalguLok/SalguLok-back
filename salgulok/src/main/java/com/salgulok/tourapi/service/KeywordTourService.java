package com.salgulok.tourapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgulok.tourapi.dto.LocationTourDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordTourService {

    private final WebClient tourApiWebClient;

    @Value("${tourapi.key}")
    private String serviceKey;

    // contentTypeId → 한글 매핑
    private String mapContentType(String typeId) {
        return switch (typeId) {
            case "12" -> "관광지";
            case "14" -> "문화시설";
            case "15" -> "축제/공연/행사";
            case "25" -> "여행코스";
            case "28" -> "레포츠";
            case "32" -> "숙박";
            case "38" -> "쇼핑";
            case "39" -> "음식";
            default -> "기타";
        };
    }

    public List<LocationTourDto> searchByKeyword(String keyword) throws JsonProcessingException {

        System.out.println("[KeywordTourService] 키워드 검색 시작");

        // keyword 인코딩
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        String url = "https://apis.data.go.kr/B551011/KorService2/searchKeyword2"
                + "?serviceKey=" + serviceKey
                + "&MobileOS=ETC"
                + "&MobileApp=TestApp"
                + "&_type=json"
                + "&keyword=" + encodedKeyword
                + "&numOfRows=" + 20
                + "&pageNo=" + 1;

        String response = tourApiWebClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("[KeywordTourService] API 응답 수신 완료");

        List<LocationTourDto> result = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode items = mapper.readTree(response)
                    .path("response").path("body").path("items").path("item");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    int contentTypeId = item.path("contenttypeid").asInt(0);
                    String contentTypeIdStr = item.path("contenttypeid").asText();
                    LocationTourDto dto = new LocationTourDto();
                    dto.setTitle(item.path("title").asText(""));
                    dto.setAddress(item.path("addr1").asText(""));
                    dto.setTel(item.path("tel").asText(null));
                    dto.setMapX(item.path("mapx").asDouble());
                    dto.setMapY(item.path("mapy").asDouble());
                    dto.setContentId(item.path("contentid").asLong(0));
                    dto.setContentTypeId(contentTypeId);
                    dto.setContentType(mapContentType(contentTypeIdStr));
                    dto.setImageUrl1(item.path("firstimage").asText(null));
                    dto.setImageUrl2(item.path("firstimage2").asText(null));
                    dto.setCopyrightCode(item.path("cpyrhtDivCd").asText(null));
                    result.add(dto);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("TourAPI 응답 파싱 실패", e);
        }

        return result;
    }
}
