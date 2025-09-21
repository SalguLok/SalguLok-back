package com.salgulok.image.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

/**
 * 클라이언트가 presigned URL로 업로드를 완료한 뒤,
 * 실제 저장된 객체 key들을 서버에 알려 DB에 반영하기 위한 요청
 */
@Getter
public class ImageConfirmRequest {

    @NotEmpty
    @Valid
    private List<Item> items;

    @Getter
    public static class Item {
        @NotEmpty
        private String objectKey;    // S3에 최종 저장된 key
        private String url;          // (선택) CloudFront/S3 퍼블릭 URL
        private String fileName;     // (선택) 원본 파일명
        private String contentType;  // (선택)
        private Long size;           // (선택)
    }

}

