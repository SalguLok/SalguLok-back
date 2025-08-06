package com.salgulok.log.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LogUpdateRequest {
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotNull(message = "공개여부를 입력해주세요.")
    private Boolean isPublic;

    @NotNull(message = "여행지역을 입력해주세요.")
    private Long regionId;

    private String imgUrl;
    private String oneReview;
}
