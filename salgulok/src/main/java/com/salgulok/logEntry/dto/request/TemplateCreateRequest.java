package com.salgulok.logEntry.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 장소별 템플릿 요청 DTO
 * - 하루 기록 내 여러 장소 각각의 글, 평점, 이미지 정보를 담는다
 */
@Getter
@NoArgsConstructor
public class TemplateCreateRequest {

    /** 장소 ID */
    @NotNull(message = "placeId는 필수입니다.")
    private Long placeId;

    private String text;

    /** 해당 장소에 대한 텍스트 기록 */
    @NotNull(message = "별점은 필수입니다.")
    private Integer star;

    /** 해당 장소에 업로드된 이미지 URL 리스트 */
    private List<String> imageUrls;
}
