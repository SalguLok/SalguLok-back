package com.salgulok.image.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ImageConfirmResponse {
    // DB에 생성된 이미지 PK들 (예: TemplateImage의 ID)
    private List<Long> imageIds;
}
