package com.salgulok.tourapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TourDetailDto {
    // 기본 정보
    private Long contentId;
    private Integer contentTypeId;
    private String title;

    // detailCommon1 정보
    private CommonInfo commonInfo;

    // detailIntro1 정보
    private IntroInfo introInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommonInfo {
        private String overview;        // 개요
        private String homepage;        // 홈페이지
        private String tel;             // 전화번호
        private String telname;         // 전화번호명
        private String addr1;           // 주소
        private String addr2;           // 상세주소
        private String zipcode;         // 우편번호
        private Double mapx;            // GPS X좌표 (경도)
        private Double mapy;            // GPS Y좌표 (위도)
        private String firstimage;      // 대표이미지 (원본)
        private String firstimage2;     // 대표이미지 (썸네일)
        private String cpyrhtDivCd;     // 저작권 유형
        private String booktour;        // 교과서 속 여행지 여부
        private String createdtime;     // 등록일
        private String modifiedtime;    // 수정일
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntroInfo {
        // 공통 소개 정보
        private String infocenter;      // 문의 및 안내
        private String restdate;        // 쉬는날
        private String usetime;         // 이용시간
        private String parking;         // 주차시설

        // 관광지 (contenttypeid=12)
        private String accomcount;      // 수용인원
        private String chkbabycarriage;  // 유모차 대여 정보
        private String chkcreditcard;   // 신용카드 가능 정보
        private String chkpet;          // 애완동물 동반 가능 정보
        private String expguide;        // 체험 안내
        private String expagerange;     // 체험 가능 연령
        private String infocenterculture; // 문화시설 문의처

        // 문화시설 (contenttypeid=14)
        private String accomcountculture; // 수용인원
        private String chkbabycarriageculture; // 유모차 대여
        private String chkcreditcardculture;   // 신용카드 가능
        private String chkpetculture;          // 애완동물 동반
        private String discountinfo;           // 할인 정보
        private String parkingculture;         // 주차시설
        private String parkingfee;             // 주차요금
        private String scale;                  // 규모
        private String spendtime;              // 관람 소요시간
        private String usetimeculture;         // 이용시간

        // 축제/공연/행사 (contenttypeid=15)
        private String agelimit;        // 관람 가능 연령
        private String bookingplace;    // 예매처
        private String discountinfofestival; // 할인 정보
        private String eventenddate;    // 행사 종료일
        private String eventhomepage;   // 행사 홈페이지
        private String eventplace;      // 행사 장소
        private String eventstartdate;  // 행사 시작일
        private String festivalgrade;   // 축제 등급
        private String placeinfo;       // 행사 장소 정보
        private String playtime;        // 공연 시간
        private String program;         // 행사 프로그램
        private String spendtimefestival; // 관람 소요시간
        private String sponsor1;        // 주최자 정보
        private String sponsor1tel;     // 주최자 연락처
        private String sponsor2;        // 주관사 정보
        private String sponsor2tel;     // 주관사 연락처
        private String subevent;        // 부대행사
        private String usetimefestival; // 이용요금

        // 여행코스 (contenttypeid=25)
        private String distance;        // 코스 총거리
        private String infocentertourcourse; // 문의 및 안내
        private String schedule;        // 코스 일정
        private String taketime;        // 코스 소요시간
        private String theme;           // 코스 테마

        // 레포츠 (contenttypeid=28)
        private String accomcountleports; // 수용인원
        private String chkbabycarriageleports; // 유모차 대여
        private String chkcreditcardleports;   // 신용카드 가능
        private String chkpetleports;          // 애완동물 동반
        private String expagerangeleports;     // 체험 가능 연령
        private String infocenterleports;      // 문의 및 안내
        private String openperiod;             // 개장 기간
        private String parkingfeeleports;      // 주차요금
        private String parkingleports;         // 주차시설
        private String reservation;            // 예약 안내
        private String restdateleports;        // 쉬는날
        private String scaleleports;           // 규모
        private String usefeeleports;          // 입장료
        private String usetimeleports;         // 이용시간

        // 숙박 (contenttypeid=32)
        private String accomcountlodging;      // 객실수
        private String benikia;                // 베니키아 여부
        private String checkintime;            // 입실 시간
        private String checkouttime;           // 퇴실 시간
        private String chkcooking;             // 객실내 취사 여부
        private String foodplace;              // 식음료장
        private String goodstay;               // 굿스테이 여부
        private String hanok;                  // 한옥 여부
        private String infocenterlodging;      // 문의 및 안내
        private String parkinglodging;         // 주차시설
        private String pickup;                 // 픽업 서비스
        private String roomcount;              // 객실수
        private String roomtype;               // 객실유형
        private String reservationlodging;     // 예약 안내
        private String reservationurl;         // 예약 안내 홈페이지
        private String subfacility;            // 부대시설

        // 쇼핑 (contenttypeid=38)
        private String chkbabycarriageshopping; // 유모차 대여
        private String chkcreditcardshopping;   // 신용카드 가능
        private String chkpetshopping;          // 애완동물 동반
        private String culturecenter;           // 문화센터 바로가기
        private String fairday;                 // 장서는 날
        private String infocentershopping;      // 문의 및 안내
        private String opendateshopping;        // 개장일
        private String opentime;                // 영업시간
        private String parkingshopping;         // 주차시설
        private String restdateshopping;        // 쉬는날
        private String restroom;                // 화장실 설명
        private String saleitem;                // 판매 품목
        private String saleitemcost;            // 판매 품목별 가격
        private String scaleshopping;           // 규모
        private String shopguide;               // 매장 안내

        // 음식 (contenttypeid=39)
        private String chkcreditcardfood;      // 신용카드 가능
        private String discountinfofood;       // 할인 정보
        private String firstmenu;              // 대표 메뉴
        private String infocenterfood;         // 문의 및 안내
        private String kidsfacility;           // 어린이 놀이방 여부
        private String opendatefood;           // 개업일
        private String opentimefood;           // 영업시간
        private String packing;                // 포장 가능
        private String parkingfood;            // 주차시설
        private String reservationfood;        // 예약 안내
        private String restdatefood;           // 쉬는날
        private String scalefood;              // 규모
        private String seat;                   // 좌석수
        private String smoking;                // 금연/흡연 여부
        private String treatmenu;              // 취급 메뉴
        private String lcnsno;                 // 인허가번호
    }
}