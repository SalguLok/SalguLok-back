package com.salgulok.logEntry.dto.response;

import com.salgulok.logEntry.domain.Template;
import com.salgulok.logEntry.domain.TemplateImage;
import com.salgulok.image.infra.ImageUrlResolver;
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
        private String resolvedUrl; // 표준화된 URL (프론트는 이 값만 사용)
        private String presignedUrl; // S3 private 버킷 접근용 presigned URL
    }

    public static TemplateUpdateResponse of(Template t, List<TemplateImage> imgs) {
        return TemplateUpdateResponse.builder()
                .templateId(t.getTemplateId())
                .placeId(t.getPlaceId())
                .text(t.getText())
                .star(t.getStar())
                .images(imgs.stream().map(i -> {
                    // resolvedUrl과 presignedUrl은 서비스에서 주입받아야 하므로 여기서는 null로 설정
                    // 실제 사용 시에는 서비스에서 ImageUrlResolver로 생성해서 주입
                    return ImageSummary.builder()
                            .imageId(i.getTemplateImageId())
                            .objectKey(i.getObjectKey())
                            .imageUrl(i.getImageUrl())
                            .resolvedUrl(null) // 서비스에서 주입 필요
                            .presignedUrl(null) // 서비스에서 주입 필요
                            .build();
                }).toList())
                .build();
    }

    public static TemplateUpdateResponse of(Template t, List<TemplateImage> imgs, ImageUrlResolver urlResolver) {
        return TemplateUpdateResponse.builder()
                .templateId(t.getTemplateId())
                .placeId(t.getPlaceId())
                .text(t.getText())
                .star(t.getStar())
                .images(imgs.stream().map(i -> {
                    String resolvedUrl = urlResolver.resolveUrlOrDefault(i.getImageUrl(), i.getObjectKey());
                    String presignedUrl = urlResolver.createPresignedGetUrl(i.getObjectKey());
                    return ImageSummary.builder()
                            .imageId(i.getTemplateImageId())
                            .objectKey(i.getObjectKey())
                            .imageUrl(i.getImageUrl())
                            .resolvedUrl(resolvedUrl)
                            .presignedUrl(presignedUrl)
                            .build();
                }).toList())
                .build();
    }
}
