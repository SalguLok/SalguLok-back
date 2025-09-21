package com.salgulok.logEntry.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TemplateSingleUpdateRequest {
    private String text;
    private Integer star;

    private List<Long> imageIds;
    private List<TemplateCreateRequest.ImageRequest> images;
}
