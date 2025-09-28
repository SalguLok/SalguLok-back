package com.salgulok.log.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 한줄평을 null로 비우는 것도 허용.
 * 프론트는 반드시 "oneReview" 키를 포함해서 보내도록 권장.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogReviewUpdateRequest {
    private String oneReview; // null 허용
}
