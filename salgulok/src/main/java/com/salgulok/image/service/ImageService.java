package com.salgulok.image.service;

import com.salgulok.image.dto.request.ImageConfirmRequest;
import com.salgulok.image.dto.request.PresignedUrlRequest;
import com.salgulok.image.dto.response.ImageConfirmResponse;
import com.salgulok.image.dto.response.PresignedUrlResponse;
import com.salgulok.user.domain.User;

public interface ImageService {
    PresignedUrlResponse issuePresignedUrls(User user, PresignedUrlRequest request);
    ImageConfirmResponse confirmUpload(User user, ImageConfirmRequest request);
    void deleteImage(User user, Long imageId);

    PresignedUrlResponse issueGetPresignedUrl(User user, String objectKey);
}
