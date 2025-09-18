package com.salgulok.log.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class LogCheckRequest {
    @NotNull(message = "시작날짜를 입력해주세요.")
    private LocalDate endDate;

    @NotNull(message = "종료날짜를 입력해주세요.")
}
