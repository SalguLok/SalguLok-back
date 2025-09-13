package com.salgulok.logEntry.dto.response;

import java.time.LocalDate;

public record DayFillState(LocalDate date, boolean hasTemplate) {}
