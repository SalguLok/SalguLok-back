package com.salgulok.log.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LogListResponse {
    List<LogResponse> logs;
}
