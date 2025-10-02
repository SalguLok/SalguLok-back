package com.salgulok.places.service;

import com.salgulok.log.domain.Log;
import com.salgulok.log.dto.response.LogResponse;
import com.salgulok.log.repository.LogRepository;
import com.salgulok.logEntry.repository.TemplateRepository;
import com.salgulok.places.domain.Place;
import com.salgulok.places.dto.response.PlaceResponseDto;
import com.salgulok.places.repository.PlaceRepository;
import com.salgulok.region.domain.Region;
import com.salgulok.region.repository.RegionRepository;
import com.salgulok.tourapi.dto.LocationTourDto;
import com.salgulok.tourapi.service.KeywordTourService;
import com.salgulok.tourapi.service.LocationTourService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final LocationTourService tourService;
    private final KeywordTourService keywordTourService;
    private final LogRepository logRepository;
    private final TemplateRepository templateRepository;
    private final RegionRepository regionRepository;

    //주소에서 regionId 매핑
    private Long mapAddrToRegionId(String addr1) {
        if (addr1 == null) return null;

        // 전처리: 남/북도 → 도 로 단순화
        String normalized = addr1;
        if (normalized.startsWith("경상남도") || normalized.startsWith("경상북도")) {
            normalized = "경상도";
        } else if (normalized.startsWith("전라남도") || normalized.startsWith("전라북도")) {
            normalized = "전라도";
        } else if (normalized.startsWith("충청남도") || normalized.startsWith("충청북도")) {
            normalized = "충청도";
        }


        List<Region> regions = regionRepository.findAll();
        return regions.stream()
                .filter(r -> addr1.startsWith(r.getName()))
                .map(Region::getRegionId)
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public int syncFromTourApi(double lat,double lng, int radius) {
        List<LocationTourDto> items=tourService.getNearbyTourInfo(lat, lng, radius);
        int savedOrUpdated=0;
        for(LocationTourDto dto:items) {
            Place p = placeRepository.findByContentId(dto.getContentId())
                    .orElseGet(Place::new);

            //신규면 contentId 세팅
            if (p.getPlaceId() == null) {
                p.setContentId(dto.getContentId());
            }

            //매핑(필요한 필드만 업데이트)
            p.setPlaceName(dto.getTitle());
            p.setAddr1(dto.getAddress());
            p.setTel(dto.getTel());
            p.setMapx(dto.getMapX());
            p.setMapy(dto.getMapY());
            p.setContentTypeId(dto.getContentTypeId());
            p.setImageUrl((dto.getImageUrl1() != null ? dto.getImageUrl1() : dto.getImageUrl2()));
            p.setOverview(null);
            if (p.getStar() == null) p.setStar(0.0);

            p.setRegionId(mapAddrToRegionId(dto.getAddress()));

            placeRepository.save(p);
            savedOrUpdated++;
        }

        return savedOrUpdated;
    }

    //평점 재계산
    @Transactional
    public void recalcAndUpdateRating(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + placeId));

        Double avg = templateRepository.avgStarByPlaceId(placeId);
        int cnt = templateRepository.countByPlaceId(placeId);

        place.updateStar(avg != null ? avg : 0.0, cnt);
    }

    //해당 장소를 포함한 공개 살구록 리스트
    @Transactional(readOnly = true)
    public List<LogResponse> getLogsByPlace(Long placeId) {

        return logRepository.findPublicLogsByPlaceId(placeId)
                .stream()
                .map(LogResponse::from)
                .toList();
    }


    @Transactional
    public int syncFromKeyword(String keyword){
        List<LocationTourDto> items;
        try{
            items=keywordTourService.searchByKeyword(keyword);
        }catch(com.fasterxml.jackson.core.JsonProcessingException e){
            throw new RuntimeException("TourApi 키워드 검색 파싱 실패",e);
        }

        int upserts=0;
        for(LocationTourDto dto:items){
            Long cid=dto.getContentId();
            if(cid==null||cid==0L) continue;

            Place p=placeRepository.findByContentId(cid).orElseGet(Place::new);
            if(p.getPlaceId()==null) p.setContentId(cid);

            if(dto.getTitle()!=null) p.setPlaceName(dto.getTitle());
            if(dto.getAddress()!=null) p.setAddr1(dto.getAddress());
            if(dto.getTel()!=null) p.setTel(dto.getTel());
            p.setMapx(dto.getMapX());
            p.setMapy(dto.getMapY());
            if(dto.getContentTypeId()!=0) p.setContentTypeId(dto.getContentTypeId());
            String img=dto.getImageUrl1()!=null?dto.getImageUrl1():dto.getImageUrl2();
            if(img!=null) p.setImageUrl(img);
            if(p.getStar()==null) p.setStar(0.0);

            p.setRegionId(mapAddrToRegionId(dto.getAddress()));

            placeRepository.save(p);
            upserts++;
        }
        return upserts;
    }
    public List<PlaceResponseDto> searchPlaces(String keyword) {
        List<Place> places = placeRepository.findByPlaceNameContaining(keyword);

        return places.stream()
                .map(PlaceResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PlaceResponseDto> getPlacesAround(double lat, double lng, int radius) {
        List<Place> allPlaces = placeRepository.findAll();

        // 위도, 경도 기준 거리 계산 (단위: 미터)
        return allPlaces.stream()
                .filter(place -> {
                    if (place.getMapx() == null || place.getMapy() == null) return false;
                    double distance = haversine(lat, lng, place.getMapy(), place.getMapx());
                    return distance <= radius;
                })
                .map(PlaceResponseDto::from)
                .collect(Collectors.toList());
    }

    // Haversine 공식: 두 좌표 간 거리 (단위: 미터)
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
        // 필요시 상위 20개 제한
        popularPlaces = popularPlaces.stream().limit(20).toList();

        // placeId 목록 추출
        List<Long> placeIds = popularPlaces.stream()
                .map(Place::getPlaceId)
                .toList();

        // 장소별 업로드+공개 로그 개수 집계
        var raw = logRepository.countUploadedPublicLogsByPlaceIds(placeIds);
        // [placeId -> cnt] 맵핑
        var countMap = raw.stream().collect(
                java.util.stream.Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                )
        );

        // DTO 변환 + logCount 주입 (없으면 0)
        return popularPlaces.stream()
                .map(p -> PlaceResponseDto.from(p, countMap.getOrDefault(p.getPlaceId(), 0L)))
                .collect(Collectors.toList());
    }

    public List<PlaceResponseDto> getPopularPlacesByRegion(Long regionId) {
        List<Place> places = placeRepository.findPopularPlacesByRegion(regionId);

        List<Long> placeIds = places.stream().map(Place::getPlaceId).toList();
        var raw = logRepository.countUploadedPublicLogsByPlaceIds(placeIds);
        var countMap = raw.stream().collect(
                java.util.stream.Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                )
        );

        return places.stream()
                .map(p -> PlaceResponseDto.from(p, countMap.getOrDefault(p.getPlaceId(), 0L)))
                .toList();
    }

    // 지역 ID → 문자열 주소 매핑 (예시용)
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
