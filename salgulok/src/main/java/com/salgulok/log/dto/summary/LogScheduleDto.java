package com.salgulok.log.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogScheduleDto {
    private Long userId;
    private Long logId;
    private Long regionId;
}
