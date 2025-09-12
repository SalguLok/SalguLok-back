package com.salgulok.image.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

/**
 * Presigned URL 발급 요청
 * - 파일들(이름/컨텐트 타입/사이즈)과, 어디에 귀속시킬지(예: templateId)를 함께 보낼 수 있게 가볍게 잡음
 * - multipart 대용량 지원 여부나 파트 개수 등은 필요해지면 필드 확장
 */
@Getter
public class PresignedUrlRequest {

    // 예: 템플릿 이미지라면 templateId, 마이페이지용이라면 null 가능
    private Long templateId;

    @NotEmpty
    @Size(max = 20, message = "한 번에 20개를 초과해 발급할 수 없습니다.")
    @Valid
    private List<FileSpec> files;

    @Getter
    public static class FileSpec {
        @NotEmpty
        private String fileName;       // 클라가 가진 원본 파일명 (확장자 포함)
        @NotEmpty
        private String contentType;    // image/jpeg, image/png 등
        private Long size;             // 바이트 (선택)
    }
}
