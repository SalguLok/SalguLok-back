// src/main/java/com/salgulok/logEntry/dto/response/FillCalendarResponse.java
package com.salgulok.logEntry.dto.response;

import java.time.LocalDate;
import java.util.List;

public record FillCalendarResponse(
        Long logId,
        LocalDate startDate,
        LocalDate endDate,
        List<DayFillState> days
) {}
