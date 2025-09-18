package com.salgulok.community.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostSearchCond {
    private Long regionId;   // ★ 추가
    private String region;   // ex) "seoul" (Region.name 또는 code로 맞춰 비교)
    private String topic;    // Topic enum name 또는 소문자 문자열
    private String status;   // "STAYING" → User.isTraveling = true
}
