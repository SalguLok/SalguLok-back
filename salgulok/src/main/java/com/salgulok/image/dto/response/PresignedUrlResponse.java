package com.salgulok.image.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PresignedUrlResponse {
    private List<Item> items;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Item {
        private String fileName;
        private String objectKey;     // S3 key (ex. images/{yyyy}/{MM}/{uuid}.jpg)
        private String presignedUrl;  // PUT(or POST) presigned URL
        private String contentType;
        private Long expiresInSec;    // 만료 초
    }
}