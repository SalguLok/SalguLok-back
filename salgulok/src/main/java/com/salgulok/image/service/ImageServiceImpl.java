package com.salgulok.image.service;

import com.salgulok.image.dto.request.ImageConfirmRequest;
import com.salgulok.image.dto.request.PresignedUrlRequest;
import com.salgulok.image.dto.response.ImageConfirmResponse;
import com.salgulok.image.dto.response.PresignedUrlResponse;
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * ImageService 구현체
 * - Presigned URL 발급
 * - 업로드 완료 확인(DB 반영)
 * - 이미지 삭제(S3 + DB)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService {

    // 예: 추후 S3Presigner, Repository 등을 주입받을 예정
    // private final S3Presigner s3Presigner;
    // private final TemplateImageRepository templateImageRepository;

    @Override
    public PresignedUrlResponse issuePresignedUrls(User user, PresignedUrlRequest request) {
        // TODO: S3 presigned URL 발급 로직
        return PresignedUrlResponse.builder()
                .items(Collections.emptyList())
                .build();
    }

    @Override
    @Transactional
    public ImageConfirmResponse confirmUpload(User user, ImageConfirmRequest request) {
        // TODO: DB 저장(TemplateImage 등) 후 생성된 PK 반환
        return ImageConfirmResponse.builder()
                .imageIds(Collections.emptyList())
                .build();
    }

    @Override
    @Transactional
    public void deleteImage(User user, Long imageId) {
        // TODO: 소유자 검증 -> S3 객체 삭제 -> 레코드 삭제
    }
}