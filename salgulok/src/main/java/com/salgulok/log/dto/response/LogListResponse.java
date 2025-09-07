package com.salgulok.log.dto.response;

import com.salgulok.log.dto.summary.LogSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LogListResponse {
    List<LogSummary> logs;
}
