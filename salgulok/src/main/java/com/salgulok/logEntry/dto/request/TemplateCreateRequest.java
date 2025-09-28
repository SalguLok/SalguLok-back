package com.salgulok.logEntry.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TemplateCreateRequest {

    @NotNull(message = "장소 ID는 필수입니다.")
    private Long placeId;

    private String text;

    private Integer star;

    private List<Long> imageIds; // 우선순위 1

    // 호환성을 위한 레거시 필드
    private List<ImageRequest> images; // 우선순위 2

    @Getter
    @Setter
    public static class ImageRequest {
        private String objectKey;
        private String url;
        private String fileName;
        private String contentType;
        private Long size;
    }
}
