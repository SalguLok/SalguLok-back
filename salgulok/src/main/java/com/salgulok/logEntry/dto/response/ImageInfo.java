package com.salgulok.logEntry.dto.response;

import com.salgulok.image.infra.ImageUrlResolver;
import com.salgulok.logEntry.domain.TemplateImage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageInfo {
    private Long imageId;
    private String objectKey;
    private String imageUrl;
    private String presignedUrl;

    public static ImageInfo of(TemplateImage image, ImageUrlResolver resolver) {
        return ImageInfo.builder()
                .imageId(image.getTemplateImageId())
                .objectKey(image.getObjectKey())
                .imageUrl(image.getImageUrl())
                .presignedUrl(resolver.createPresignedGetUrl(image.getObjectKey()))
                .build();
    }
}
