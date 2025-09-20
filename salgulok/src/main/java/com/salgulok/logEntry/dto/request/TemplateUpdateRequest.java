package com.salgulok.logEntry.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 템플릿 하나의 수정 요청 정보
 * → 기존 템플릿 ID 기준으로 텍스트/별점/이미지 수정
 */
@Getter
@Setter
public class TemplateUpdateRequest {
    private Long templateId;
    private String text;
    private int star;
    private List<TemplateCreateRequest.ImageRequest> images;
}
