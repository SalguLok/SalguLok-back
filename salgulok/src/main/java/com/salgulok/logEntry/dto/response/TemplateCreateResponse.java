package com.salgulok.logEntry.dto.response;

import com.salgulok.image.infra.ImageUrlResolver;
import com.salgulok.logEntry.domain.Template;
import com.salgulok.logEntry.domain.TemplateImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TemplateCreateResponse {
    private Long templateId;
    private Long placeId;
    private String text;
    private Integer star;
    private List<ImageInfo> images;

    public static TemplateCreateResponse of(Template template, List<TemplateImage> images, ImageUrlResolver resolver) {
        return TemplateCreateResponse.builder()
                .templateId(template.getTemplateId())
                .placeId(template.getPlaceId())
                .text(template.getText())
                .star(template.getStar())
                .images(images.stream().map(image -> ImageInfo.of(image, resolver)).toList())
                .build();
    }
}
