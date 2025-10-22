package com.salgulok.tourapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgulok.tourapi.dto.TourDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TourDetailService {

    private final WebClient tourApiWebClient;

    @Value("${tourapi.key}")
    private String serviceKey;

    /**
     * contentId와 contentTypeId로 상세 정보 조회 (detailCommon1 + detailIntro1 통합)
     * contentTypeId가 null이면 detailIntro1은 호출하지 않음
     */
    public TourDetailDto getTourDetail(Long contentId, Integer contentTypeId) {
        System.out.println("[TourDetailService] 상세 정보 조회 시작 - contentId: " + contentId + ", contentTypeId: " + contentTypeId);

        TourDetailDto result = new TourDetailDto();
        result.setContentId(contentId);
        result.setContentTypeId(contentTypeId);

        // commonInfo는 항상 조회
        CompletableFuture<TourDetailDto.CommonInfo> commonFuture =
                CompletableFuture.supplyAsync(() -> getDetailCommon(contentId));

        // contentTypeId가 있을 때만 introInfo 조회
        CompletableFuture<TourDetailDto.IntroInfo> introFuture = null;
        if (contentTypeId != null) {
            introFuture = CompletableFuture.supplyAsync(() -> getDetailIntro(contentId, contentTypeId));
            CompletableFuture.allOf(commonFuture, introFuture).join();
        } else {
            commonFuture.join();
        }

        // 결과 취합
        result.setCommonInfo(commonFuture.join());
        if (introFuture != null) {
            result.setIntroInfo(introFuture.join());
        }

        // title은 commonInfo에서 가져오기
        if (result.getCommonInfo() != null) {
            result.setTitle(result.getCommonInfo().getAddr1());
        }

        System.out.println("[TourDetailService] 상세 정보 조회 완료");
        return result;
    }

    /**
     * detailCommon1 API 호출 - 공통 정보
     */
    private TourDetailDto.CommonInfo getDetailCommon(Long contentId) {
        String url = "https://apis.data.go.kr/B551011/KorService2/detailCommon1"
                + "?serviceKey=" + serviceKey
                + "&MobileOS=ETC"
                + "&MobileApp=SalguLok"
                + "&contentId=" + contentId
                + "&defaultYN=Y"
                + "&firstImageYN=Y"
                + "&areacodeYN=Y"
                + "&addrinfoYN=Y"
                + "&overviewYN=Y"
                + "&_type=json";

        try {
            String response = tourApiWebClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseCommonInfo(response);
        } catch (Exception e) {
            System.err.println("[TourDetailService] detailCommon1 호출 실패: " + e.getMessage());
            return new TourDetailDto.CommonInfo();
        }
    }

    /**
     * detailIntro1 API 호출 - 소개 정보
     */
    private TourDetailDto.IntroInfo getDetailIntro(Long contentId, Integer contentTypeId) {
        String url = "https://apis.data.go.kr/B551011/KorService2/detailIntro1"
                + "?serviceKey=" + serviceKey
                + "&MobileOS=ETC"
                + "&MobileApp=SalguLok"
                + "&contentId=" + contentId
                + "&contentTypeId=" + contentTypeId
                + "&_type=json";

        try {
            String response = tourApiWebClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseIntroInfo(response, contentTypeId);
        } catch (Exception e) {
            System.err.println("[TourDetailService] detailIntro1 호출 실패: " + e.getMessage());
            return new TourDetailDto.IntroInfo();
        }
    }

    /**
     * detailCommon1 응답 파싱
     */
    private TourDetailDto.CommonInfo parseCommonInfo(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode item = mapper.readTree(response)
                    .path("response")
                    .path("body")
                    .path("items")
                    .path("item");

            // 배열인 경우 첫 번째 요소 선택
            if (item.isArray() && item.size() > 0) {
                item = item.get(0);
            }

            TourDetailDto.CommonInfo info = new TourDetailDto.CommonInfo();
            info.setOverview(item.path("overview").asText(null));
            info.setHomepage(item.path("homepage").asText(null));
            info.setTel(item.path("tel").asText(null));
            info.setTelname(item.path("telname").asText(null));
            info.setAddr1(item.path("addr1").asText(null));
            info.setAddr2(item.path("addr2").asText(null));
            info.setZipcode(item.path("zipcode").asText(null));
            info.setMapx(item.path("mapx").asDouble(0.0));
            info.setMapy(item.path("mapy").asDouble(0.0));
            info.setFirstimage(item.path("firstimage").asText(null));
            info.setFirstimage2(item.path("firstimage2").asText(null));
            info.setCpyrhtDivCd(item.path("cpyrhtDivCd").asText(null));
            info.setBooktour(item.path("booktour").asText(null));
            info.setCreatedtime(item.path("createdtime").asText(null));
            info.setModifiedtime(item.path("modifiedtime").asText(null));

            return info;
        } catch (Exception e) {
            throw new RuntimeException("detailCommon1 응답 파싱 실패", e);
        }
    }

    /**
     * detailIntro1 응답 파싱
     */
    private TourDetailDto.IntroInfo parseIntroInfo(String response, Integer contentTypeId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode item = mapper.readTree(response)
                    .path("response")
                    .path("body")
                    .path("items")
                    .path("item");

            // 배열인 경우 첫 번째 요소 선택
            if (item.isArray() && item.size() > 0) {
                item = item.get(0);
            }

            TourDetailDto.IntroInfo info = new TourDetailDto.IntroInfo();

            // 공통 필드
            info.setInfocenter(item.path("infocenter").asText(null));
            info.setRestdate(item.path("restdate").asText(null));
            info.setUsetime(item.path("usetime").asText(null));
            info.setParking(item.path("parking").asText(null));

            // contentTypeId별 특화 필드 파싱
            switch (contentTypeId) {
                case 12: // 관광지
                    parseTypeSpecific12(item, info);
                    break;
                case 14: // 문화시설
                    parseTypeSpecific14(item, info);
                    break;
                case 15: // 축제/공연/행사
                    parseTypeSpecific15(item, info);
                    break;
                case 25: // 여행코스
                    parseTypeSpecific25(item, info);
                    break;
                case 28: // 레포츠
                    parseTypeSpecific28(item, info);
                    break;
                case 32: // 숙박
                    parseTypeSpecific32(item, info);
                    break;
                case 38: // 쇼핑
                    parseTypeSpecific38(item, info);
                    break;
                case 39: // 음식
                    parseTypeSpecific39(item, info);
                    break;
            }

            return info;
        } catch (Exception e) {
            throw new RuntimeException("detailIntro1 응답 파싱 실패", e);
        }
    }

    // 관광지 (12)
    private void parseTypeSpecific12(JsonNode item, TourDetailDto.IntroInfo info) {
        info.setAccomcount(item.path("accomcount").asText(null));
        info.setChkbabycarriage(item.path("chkbabycarriage").asText(null));
        info.setChkcreditcard(item.path("chkcreditcard").asText(null));
        info.setChkpet(item.path("chkpet").asText(null));
        info.setExpguide(item.path("expguide").asText(null));
        info.setExpagerange(item.path("expagerange").asText(null));
        info.setInfocenterculture(item.path("infocenter").asText(null));
    }

    // 문화시설 (14)
    private void parseTypeSpecific14(JsonNode item, TourDetailDto.IntroInfo info) {
        info.setAccomcountculture(item.path("accomcountculture").asText(null));
        info.setChkbabycarriageculture(item.path("chkbabycarriageculture").asText(null));
        info.setChkcreditcardculture(item.path("chkcreditcardculture").asText(null));
        info.setChkpetculture(item.path("chkpetculture").asText(null));
        info.setDiscountinfo(item.path("discountinfo").asText(null));
        info.setParkingculture(item.path("parkingculture").asText(null));
        info.setParkingfee(item.path("parkingfee").asText(null));
        info.setScale(item.path("scale").asText(null));
        info.setSpendtime(item.path("spendtime").asText(null));
        info.setUsetimeculture(item.path("usetimeculture").asText(null));
    }

    // 축제/공연/행사 (15)
    private void parseTypeSpecific15(JsonNode item, TourDetailDto.IntroInfo info) {
        info.setAgelimit(item.path("agelimit").asText(null));
        info.setBookingplace(item.path("bookingplace").asText(null));
        info.setDiscountinfofestival(item.path("discountinfofestival").asText(null));
        info.setEventenddate(item.path("eventenddate").asText(null));
        info.setEventhomepage(item.path("eventhomepage").asText(null));
        info.setEventplace(item.path("eventplace").asText(null));
        info.setEventstartdate(item.path("eventstartdate").asText(null));
        info.setFestivalgrade(item.path("festivalgrade").asText(null));
        info.setPlaceinfo(item.path("placeinfo").asText(null));
        info.setPlaytime(item.path("playtime").asText(null));
        info.setProgram(item.path("program").asText(null));
        info.setSpendtimefestival(item.path("spendtimefestival").asText(null));
        info.setSponsor1(item.path("sponsor1").asText(null));
        info.setSponsor1tel(item.path("sponsor1tel").asText(null));
        info.setSponsor2(item.path("sponsor2").asText(null));
        info.setSponsor2tel(item.path("sponsor2tel").asText(null));
        info.setSubevent(item.path("subevent").asText(null));
        info.setUsetimefestival(item.path("usetimefestival").asText(null));
    }

    // 여행코스 (25)
    private void parseTypeSpecific25(JsonNode item, TourDetailDto.IntroInfo info) {
        info.setDistance(item.path("distance").asText(null));
        info.setInfocentertourcourse(item.path("infocentertourcourse").asText(null));
        info.setSchedule(item.path("schedule").asText(null));
        info.setTaketime(item.path("taketime").asText(null));
        info.setTheme(item.path("theme").asText(null));
    }

    // 레포츠 (28)
    private void parseTypeSpecific28(JsonNode item, TourDetailDto.IntroInfo info) {
        info.setAccomcountleports(item.path("accomcountleports").asText(null));
        info.setChkbabycarriageleports(item.path("chkbabycarriageleports").asText(null));
        info.setChkcreditcardleports(item.path("chkcreditcardleports").asText(null));
        info.setChkpetleports(item.path("chkpetleports").asText(null));
        info.setExpagerangeleports(item.path("expagerangeleports").asText(null));
        info.setInfocenterleports(item.path("infocenterleports").asText(null));
        info.setOpenperiod(item.path("openperiod").asText(null));
        info.setParkingfeeleports(item.path("parkingfeeleports").asText(null));
        info.setParkingleports(item.path("parkingleports").asText(null));
        info.setReservation(item.path("reservation").asText(null));
        info.setRestdateleports(item.path("restdateleports").asText(null));
        info.setScaleleports(item.path("scaleleports").asText(null));
        info.setUsefeeleports(item.path("usefeeleports").asText(null));
        info.setUsetimeleports(item.path("usetimeleports").asText(null));
    }

    // 숙박 (32)
    private void parseTypeSpecific32(JsonNode item, TourDetailDto.IntroInfo info) {
        info.setAccomcountlodging(item.path("accomcountlodging").asText(null));
        info.setBenikia(item.path("benikia").asText(null));
        info.setCheckintime(item.path("checkintime").asText(null));
        info.setCheckouttime(item.path("checkouttime").asText(null));
        info.setChkcooking(item.path("chkcooking").asText(null));
        info.setFoodplace(item.path("foodplace").asText(null));
        info.setGoodstay(item.path("goodstay").asText(null));
        info.setHanok(item.path("hanok").asText(null));
        info.setInfocenterlodging(item.path("infocenterlodging").asText(null));
        info.setParkinglodging(item.path("parkinglodging").asText(null));
        info.setPickup(item.path("pickup").asText(null));
        info.setRoomcount(item.path("roomcount").asText(null));
        info.setRoomtype(item.path("roomtype").asText(null));
        info.setReservationlodging(item.path("reservationlodging").asText(null));
        info.setReservationurl(item.path("reservationurl").asText(null));
        info.setSubfacility(item.path("subfacility").asText(null));
    }

    // 쇼핑 (38)
    private void parseTypeSpecific38(JsonNode item, TourDetailDto.IntroInfo info) {
        info.setChkbabycarriageshopping(item.path("chkbabycarriageshopping").asText(null));
        info.setChkcreditcardshopping(item.path("chkcreditcardshopping").asText(null));
        info.setChkpetshopping(item.path("chkpetshopping").asText(null));
        info.setCulturecenter(item.path("culturecenter").asText(null));
        info.setFairday(item.path("fairday").asText(null));
        info.setInfocentershopping(item.path("infocentershopping").asText(null));
        info.setOpendateshopping(item.path("opendateshopping").asText(null));
        info.setOpentime(item.path("opentime").asText(null));
        info.setParkingshopping(item.path("parkingshopping").asText(null));
        info.setRestdateshopping(item.path("restdateshopping").asText(null));
        info.setRestroom(item.path("restroom").asText(null));
        info.setSaleitem(item.path("saleitem").asText(null));
        info.setSaleitemcost(item.path("saleitemcost").asText(null));
        info.setScaleshopping(item.path("scaleshopping").asText(null));
        info.setShopguide(item.path("shopguide").asText(null));
    }

    // 음식 (39)
    private void parseTypeSpecific39(JsonNode item, TourDetailDto.IntroInfo info) {
        info.setChkcreditcardfood(item.path("chkcreditcardfood").asText(null));
        info.setDiscountinfofood(item.path("discountinfofood").asText(null));
        info.setFirstmenu(item.path("firstmenu").asText(null));
        info.setInfocenterfood(item.path("infocenterfood").asText(null));
        info.setKidsfacility(item.path("kidsfacility").asText(null));
        info.setOpendatefood(item.path("opendatefood").asText(null));
        info.setOpentimefood(item.path("opentimefood").asText(null));
        info.setPacking(item.path("packing").asText(null));
        info.setParkingfood(item.path("parkingfood").asText(null));
        info.setReservationfood(item.path("reservation").asText(null));
        info.setRestdatefood(item.path("restdatefood").asText(null));
        info.setScalefood(item.path("scalefood").asText(null));
        info.setSeat(item.path("seat").asText(null));
        info.setSmoking(item.path("smoking").asText(null));
        info.setTreatmenu(item.path("treatmenu").asText(null));
        info.setLcnsno(item.path("lcnsno").asText(null));
    }
}