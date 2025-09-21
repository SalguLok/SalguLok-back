package com.salgulok.logEntry.dto.response;

import com.salgulok.logEntry.domain.Template;
import com.salgulok.logEntry.domain.TemplateImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TemplateUpdateResponse {
    private long templateId;
    private Long placeId;
    private String text;
    private int star;
    private List<ImageSummary> images;

    @Getter
    @Builder
    public static class ImageSummary {
        private Long imageId;
        private String objectKey;
        private String imageUrl;
    }

    public static TemplateUpdateResponse of(Template t, List<TemplateImage> imgs) {
        return TemplateUpdateResponse.builder()
                .templateId(t.getTemplateId())
                .placeId(t.getPlaceId())
                .text(t.getText())
                .star(t.getStar())
                .images(imgs.stream().map(i ->
                        ImageSummary.builder()
                                .imageId(i.getTemplateImageId())
                                .objectKey(i.getObjectKey())
                                .imageUrl(i.getImageUrl())
                                .build()
                ).toList())
                .build();
    }
}
