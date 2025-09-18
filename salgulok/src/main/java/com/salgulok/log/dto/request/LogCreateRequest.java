package com.salgulok.log.dto.request;

import com.salgulok.log.domain.Log;
import com.salgulok.region.domain.Region;
import com.salgulok.user.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class LogCreateRequest {
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotNull(message = "시작날짜를 입력해주세요.")
    private LocalDate startDate;

    @NotNull(message = "종료날짜를 입력해주세요.")
    private LocalDate endDate;

    @NotNull(message = "공개여부를 입력해주세요.")
    private Boolean isPublic;

    @NotNull(message = "여행지역을 입력해주세요.")
    private Long regionId;

    private String imgUrl;
    private String oneReview;

    public Log toEntity(User user, Region region){
        return Log.builder()
                .user(user)
                .region(region)
                .title(title)
                .imgUrl(imgUrl)
                .oneReview(oneReview)
                .startDate(startDate)
                .endDate(endDate)
                .isPublic(isPublic)
                .build();
    }
}