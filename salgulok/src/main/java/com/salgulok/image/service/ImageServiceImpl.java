package com.salgulok.image.service;

import com.salgulok.image.dto.request.ImageConfirmRequest;
import com.salgulok.image.dto.request.PresignedUrlRequest;
import com.salgulok.image.dto.response.ImageConfirmResponse;
import com.salgulok.image.dto.response.PresignedUrlResponse;
import com.salgulok.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final Duration EXPIRY = Duration.ofMinutes(10);

    @Override
    public PresignedUrlResponse issuePresignedUrls(User user, PresignedUrlRequest request) {
        List<PresignedUrlResponse.Item> items = new ArrayList<>();

        for (PresignedUrlRequest.FileSpec f : request.getFiles()) {
            String ext = getExt(f.getFileName());                 // ".png"
            String key = buildKey(user.getUserId(), ext);         // images/2025/09/17/{userId}/{uuid}.png

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(f.getContentType())
                    //.acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                    .signatureDuration(EXPIRY)
                    .putObjectRequest(putReq)
                    .build();

            PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignReq);

            items.add(PresignedUrlResponse.Item.builder()
                    .fileName(f.getFileName())
                    .objectKey(key)
                    .presignedUrl(presigned.url().toString())     // ← DTO 필드명에 맞춤
                    .contentType(f.getContentType())
                    .expiresInSec(EXPIRY.toSeconds())
                    .build());
        }

        return PresignedUrlResponse.builder().items(items).build();
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

    private String getExt(String fileName) {
        int i = fileName.lastIndexOf('.');
        return (i >= 0) ? fileName.substring(i) : "";
    }

    private String buildKey(Long userId, String ext) {
        LocalDate today = LocalDate.now();
        return String.format("images/%d/%02d/%02d/%d/%s%s",
                today.getYear(), today.getMonthValue(), today.getDayOfMonth(),
                userId, UUID.randomUUID(), ext);
    }

    // ImageServiceImpl.java
    @Override
    public PresignedUrlResponse issueGetPresignedUrl(User user, String objectKey) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))  // 5분짜리 링크
                .getObjectRequest(getReq)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignReq);

        PresignedUrlResponse.Item item = PresignedUrlResponse.Item.builder()
                .fileName(null) // 다운로드할 때만 필요하면 세팅
                .objectKey(objectKey)
                .presignedUrl(presigned.url().toString())
                .contentType("image/*")
                .expiresInSec(Duration.ofMinutes(5).toSeconds())
                .build();

        return PresignedUrlResponse.builder()
                .items(Collections.singletonList(item))
                .build();
    }

}