package com.salgulok.region.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgulok.region.domain.Region;
import com.salgulok.region.repository.RegionRepository;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RegionService {

    private final WebClient tourApiWebClient; // baseUrl 세팅된 WebClient 자동 주입

    @Value("${tourapi.key}")
    private String serviceKey;

    private final RegionRepository regionRepository;





    // TourAPI에서 지역 코드 불러와 DB 저장
    public void syncRegions() {

        // 함수 호출 후 실행되는지 확인
        System.out.println("[RegionService] 지역 코드 동기화 시작");

        String url = "https://apis.data.go.kr/B551011/KorService2/areaCode2"
                + "?serviceKey=" + serviceKey
                + "&MobileOS=ETC&MobileApp=MyApp&_type=json&numOfRows=20";

        String response = tourApiWebClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String.class)
                .block();


        // 수신된 응답 확인
        System.out.println("[RegionService] API 응답 수신 완료");
        if (response != null) {
            System.out.println("[RegionService] 응답 내용 (앞부분): " + response.substring(0, Math.min(300, response.length())));
        } else {
            System.out.println("[RegionService] 응답이 null입니다.");
        }

        List<Region> regions =
                parseRegionResponse(response);

        System.out.println("[RegionService] 파싱 완료: " + regions.size() + "개 지역 코드");

        regionRepository.saveAll(regions);  // DB에 저장
        System.out.println("[RegionService] 지역 코드 DB 저장 완료");


    }


    private List<Region> parseRegionResponse(String json) {
        ObjectMapper mapper = new ObjectMapper();
        List<Region> regions = new ArrayList<>();

        try {
            if (json == null || !json.trim().startsWith("{")) {
                System.out.println("[RegionService] JSON 파싱 실패: 응답이 비어있거나 잘못된 형식");
                throw new RuntimeException("유효하지 않은 API 응답 형식");
            }

            JsonNode root = mapper.readTree(json)
                    .path("response").path("body").path("items").path("item");

            if (!root.isArray()) {
                System.out.println("[RegionService] item 노드가 배열이 아닙니다. 실제 값: " + root);
            }

            for (JsonNode node : root) {
                int areaCode = node.path("code").asInt();
                String name = node.path("name").asText();
                System.out.println("[RegionService] 파싱된 지역: code=" + areaCode + ", name=" + name);

                regions.add(new Region(areaCode, name));

            }
        } catch (Exception e) {
            System.out.println("[RegionService] 지역 코드 파싱 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("지역 코드 파싱 중 오류 발생", e);
        }

        return regions;
    }

    //전체 region 조회 메서드
    public List<Region> getAllRegions() {
        return regionRepository.findAll(); // DB에서 모든 지역 정보 조회
    }
}

