package com.salgulok.image.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

/**
 * 이미지 URL 해석기
 * - S3 objectKey를 기반으로 퍼블릭 URL 생성
 * - CloudFront 도메인이 있으면 우선 사용, 없으면 S3 직접 URL 사용
 */
@Component
@RequiredArgsConstructor
public class ImageUrlResolver {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region}")
    private String region;

    // CloudFront 도메인이 있다면 여기에 설정 (선택사항)
    @Value("${cloud.aws.cloudfront.domain:#{null}}")
    private String cloudfrontDomain;

    private static final Duration PRESIGNED_URL_EXPIRY = Duration.ofHours(1);

    /**
     * objectKey를 기반으로 퍼블릭 URL 생성
     * 
     * @param objectKey S3 객체 키
     * @return 퍼블릭 접근 가능한 URL
     */
    public String resolveUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("objectKey cannot be null or blank");
        }

        // CloudFront 도메인이 있으면 우선 사용
        if (cloudfrontDomain != null && !cloudfrontDomain.isBlank()) {
            return String.format("https://%s/%s", cloudfrontDomain, objectKey);
        }

        // S3 직접 URL 사용
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, objectKey);
    }

    /**
     * 기존 URL이 있으면 그대로, 없으면 objectKey로 생성
     * 
     * @param existingUrl 기존 URL (null 가능)
     * @param objectKey S3 객체 키
     * @return 최종 URL
     */
    public String resolveUrlOrDefault(String existingUrl, String objectKey) {
        if (existingUrl != null && !existingUrl.isBlank()) {
            return existingUrl;
        }
        return resolveUrl(objectKey);
    }

    /**
     * objectKey를 기반으로 presigned GET URL 생성
     * - S3 private 버킷에서 임시 접근 권한을 가진 URL 생성
     * - 서명에 Content-Type을 포함하지 않음 (요구사항)
     * 
     * @param objectKey S3 객체 키
     * @return presigned GET URL
     */
    public String createPresignedGetUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("objectKey cannot be null or blank");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_EXPIRY)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }
}
