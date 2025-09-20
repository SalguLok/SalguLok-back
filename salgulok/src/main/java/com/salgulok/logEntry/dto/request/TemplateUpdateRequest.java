package com.salgulok.logEntry.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 템플릿 하나의 수정 요청 정보
 * → 기존 템플릿 ID 기준으로 텍스트/별점/이미지 수정
 */
@Getter
@Setter
@NoArgsConstructor
public class TemplateUpdateRequest {

    @NotNull(message = "templateId는 필수입니다.")
    private Long templateId;

    private String text;

    @NotNull(message = "별점은 필수입니다.")
    private Integer star;

    /** 신규 권장: /images/confirm 응답의 PK 리스트 */
    private List<Long> imageIds;

    /** 호환: 구방식(objectKey/url/...) */
    private List<TemplateCreateRequest.ImageRequest> images;
}
