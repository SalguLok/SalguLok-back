package com.salgulok.image.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

/**
 * AWS S3 Presigner & Client 파사드 클래스
 * - Presigned URL 발급
 * - 객체 삭제
 */
@Component
@RequiredArgsConstructor
public class S3ClientFacade {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;
    private final S3Presigner presigner;

    /**
     * Presigned PUT URL 발급
     *
     * @param objectKey    업로드할 파일의 key (폴더 경로 포함 가능)
     * @param contentType  Content-Type (예: image/jpeg)
     * @param expireMinutes Presigned URL 만료 시간 (분 단위)
     * @return Presigned URL 문자열
     */
    public String generatePresignedUrl(String objectKey, String contentType, int expireMinutes) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expireMinutes))
                .putObjectRequest(objectRequest)
                .build();

        return presigner.presignPutObject(presignRequest).url().toString();
    }

    /**
     * S3 객체 삭제
     *
     * @param objectKey 삭제할 파일의 key
     */
    public void deleteObject(String objectKey) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
        s3Client.deleteObject(deleteRequest);
    }
}