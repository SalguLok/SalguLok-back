package com.salgulok.image.service;

import com.salgulok.image.domain.ImageMeta;
import com.salgulok.image.dto.request.ImageConfirmRequest;
import com.salgulok.image.dto.request.PresignedUrlRequest;
import com.salgulok.image.dto.response.ImageConfirmResponse;
import com.salgulok.image.dto.response.PresignedUrlResponse;
import com.salgulok.image.infra.ImageUrlResolver;
import com.salgulok.image.repository.ImageMetaRepository;
import com.salgulok.image.service.async.ImageResizeService;
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
    private final ImageMetaRepository imageMetaRepository;
    private final ImageUrlResolver imageUrlResolver;
    private final ImageResizeService imageResizeService;

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
                // GET presigned URL은 헤더 없는 <img src> 요청을 지원해야 하므로 contentType은 서명/헤더에 포함하지 않음
                .contentType(null)
                .expiresInSec(Duration.ofMinutes(5).toSeconds())
                .build();

        return PresignedUrlResponse.builder()
                .items(Collections.singletonList(item))
                .build();
    }

    @Override
    @Transactional
    public ImageConfirmResponse confirmUpload(User user, ImageConfirmRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("No items to confirm");
        }

        Long userId = user.getUserId();
        List<Long> ids = new ArrayList<>();

        for (var item : request.getItems()) {
            if (item.getObjectKey() == null || item.getObjectKey().isBlank()) {
                throw new IllegalArgumentException("objectKey is required");
            }

            // URL이 없으면 objectKey로 자동 생성
            String resolvedUrl = imageUrlResolver.resolveUrlOrDefault(item.getUrl(), item.getObjectKey());

            // 멱등 처리: (user, objectKey) 중복 confirm 시 기존 레코드 재사용
            var meta = imageMetaRepository
                    .findByUser_UserIdAndObjectKey(userId, item.getObjectKey())
                    .orElseGet(() -> imageMetaRepository.save(
                            ImageMeta.builder()
                                    .user(user)
                                    .objectKey(item.getObjectKey())
                                    .url(resolvedUrl) // 자동 생성된 URL 사용
                                    .fileName(item.getFileName())
                                    .contentType(item.getContentType())
                                    .size(item.getSize())
                                    .status(ImageMeta.Status.CONFIRMED)
                                    .build()
                    ));

            // 썸네일 생성
            if (meta.getThumbnailStatus() == ImageMeta.ThumbnailStatus.EMPTY) {
                meta.markThumbnailPending();
                imageResizeService.resizeAndUpload(meta.getId());
            }

            ids.add(meta.getId());
        }

        return ImageConfirmResponse.builder()
                .imageIds(ids)
                .build();
    }

}