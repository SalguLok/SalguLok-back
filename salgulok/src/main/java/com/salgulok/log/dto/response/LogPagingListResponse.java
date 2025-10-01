package com.salgulok.log.dto.response;

import com.salgulok.log.domain.Log;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class LogPagingListResponse {
    private List<LogResponse> logs;
    private int totalPages;
    private int currentPage;
    private int length;

    public LogPagingListResponse(Page<LogResponse> page) {
        this.logs = page.getContent();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber();
        this.length = page.getNumberOfElements();
    }
}