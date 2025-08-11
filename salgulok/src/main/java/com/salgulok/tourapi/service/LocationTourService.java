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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationTourService {

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

    public List<LocationTourDto> getNearbyTourInfo(double lat, double lng, int radius) {

        System.out.println("[LocationTourService] 위치 기반 추천 시작");

        String url = "https://apis.data.go.kr/B551011/KorService2/locationBasedList2"
                + "?serviceKey=" + serviceKey
                + "&MobileOS=ETC"
                + "&MobileApp=SalguLok"
                + "&mapX=" + lng
                + "&mapY=" + lat
                + "&radius=" + radius
                + "&arrange=E"
                + "&numOfRows=20"
                + "&_type=json";


        String response = tourApiWebClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String.class)
                .block();


        // JSON 파싱 후 DTO 리스트 변환
        System.out.println("[LocationTourService] 리스트 반환 시작");

        List<LocationTourDto> result = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode items = mapper.readTree(response)
                    .path("response").path("body").path("items").path("item");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    int contentTypeId = item.path("contenttypeid").asInt(0);
                    String contentTypeIdStr = item.path("contenttypeid").asText();  // "14" 같은 문자열
                    LocationTourDto dto = new LocationTourDto();
                    dto.setTitle(item.path("title").asText(""));
                    dto.setAddress(item.path("addr1").asText(""));
                    dto.setTel(item.path("tel").asText(null));
                    dto.setMapX(item.path("mapx").asDouble());
                    dto.setMapY(item.path("mapy").asDouble());
                    dto.setDistance(item.path("dist").asDouble(0.0));
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
