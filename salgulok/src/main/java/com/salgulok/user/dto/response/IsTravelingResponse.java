package com.salgulok.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IsTravelingResponse {
    Boolean traveling;
    Long logId; //traveling이 false면 logId null 반환
}
