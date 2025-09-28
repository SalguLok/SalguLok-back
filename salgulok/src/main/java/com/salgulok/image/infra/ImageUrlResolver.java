package com.salgulok.image.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 이미지 URL 해석기
 * - S3 objectKey를 기반으로 퍼블릭 URL 생성
 * - CloudFront 도메인이 있으면 우선 사용, 없으면 S3 직접 URL 사용
 */
@Component
@RequiredArgsConstructor
public class ImageUrlResolver {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region}")
    private String region;

    // CloudFront 도메인이 있다면 여기에 설정 (선택사항)
    @Value("${cloud.aws.cloudfront.domain:#{null}}")
    private String cloudfrontDomain;

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
}
