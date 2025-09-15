package com.salgulok.image.controller;

import com.salgulok.image.dto.request.ImageConfirmRequest;
import com.salgulok.image.dto.request.PresignedUrlRequest;
import com.salgulok.image.dto.response.ImageConfirmResponse;
import com.salgulok.image.dto.response.PresignedUrlResponse;
import com.salgulok.image.service.ImageService;
import com.salgulok.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/presigned")
    public ResponseEntity<PresignedUrlResponse> issuePresignedUrls(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid PresignedUrlRequest request
    ) {
        PresignedUrlResponse response = imageService.issuePresignedUrls(user, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<ImageConfirmResponse> confirmUpload(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ImageConfirmRequest request
    ) {
        ImageConfirmResponse response = imageService.confirmUpload(user, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @AuthenticationPrincipal User user,
            @PathVariable Long imageId
    ) {
        imageService.deleteImage(user, imageId);
        return ResponseEntity.noContent().build();
    }

}

