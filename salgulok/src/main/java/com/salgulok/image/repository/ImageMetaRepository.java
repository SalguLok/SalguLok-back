package com.salgulok.image.repository;

import com.salgulok.image.domain.ImageMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageMetaRepository extends JpaRepository<ImageMeta, Long> {
    Optional<ImageMeta> findByUser_UserIdAndObjectKey(Long userId, String objectKey);
}
