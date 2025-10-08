package com.salgulok.image.service.async;

import com.salgulok.image.domain.ImageMeta;
import com.salgulok.image.infra.ImageUrlResolver;
import com.salgulok.image.repository.ImageMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageResizeServiceImpl implements ImageResizeService {

    private final ImageMetaRepository imageMetaRepository;
    private final S3Client s3Client;
    private final ImageUrlResolver imageUrlResolver;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;

    @Async
    @Transactional
    @Override
    public void resizeAndUpload(Long imageMetaId) {
        log.info("Starting async thumbnail generation for imageMetaId: {}", imageMetaId);

        ImageMeta imageMeta = imageMetaRepository.findById(imageMetaId)
                .orElse(null);

        if (imageMeta == null) {
            log.warn("ImageMeta not found for id: {}. Aborting thumbnail generation.", imageMetaId);
            return;
        }

        if (imageMeta.getThumbnailStatus() != ImageMeta.ThumbnailStatus.PENDING) {
            log.warn("Thumbnail status is not PENDING for id: {}. Status is {}. Aborting.", imageMetaId, imageMeta.getThumbnailStatus());
            return;
        }

        try {
            // 1. Download original image from S3
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(imageMeta.getObjectKey())
                    .build();
            ResponseInputStream<GetObjectResponse> s3object = s3Client.getObject(getRequest);

            // 2. Resize image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(s3object)
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .toOutputStream(outputStream);
            
            byte[] resizedImageBytes = outputStream.toByteArray();
            InputStream resizedInputStream = new ByteArrayInputStream(resizedImageBytes);

            // 3. Upload thumbnail to S3
            String thumbnailKey = createThumbnailKey(imageMeta.getObjectKey());
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(thumbnailKey)
                    .contentType(imageMeta.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(resizedInputStream, resizedImageBytes.length));

            // 4. Update ImageMeta entity
            String thumbnailUrl = imageUrlResolver.resolveUrlOrDefault(null, thumbnailKey);
            imageMeta.updateThumbnailInfo(thumbnailKey, thumbnailUrl);
            log.info("Successfully generated thumbnail for imageMetaId: {}", imageMetaId);

        } catch (IOException e) {
            log.error("Failed to resize image for imageMetaId: {}", imageMetaId, e);
            imageMeta.markThumbnailFailed();
        } catch (Exception e) {
            log.error("An unexpected error occurred during thumbnail generation for imageMetaId: {}", imageMetaId, e);
            imageMeta.markThumbnailFailed();
        }
    }

    private String createThumbnailKey(String originalKey) {
        // Example: images/2025/09/17/1/uuid.png -> thumbnails/2025/09/17/1/uuid.png
        if (originalKey.startsWith("images/")) {
            return "thumbnails/" + originalKey.substring("images/".length());
        }
        // Fallback for unexpected key structure
        return "thumbnails/" + originalKey;
    }
}
