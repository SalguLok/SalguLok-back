package com.salgulok.logEntry.dto.request;

import java.util.List;

/**
 * 템플릿 하나의 수정 요청 정보
 * → 기존 템플릿 ID 기준으로 텍스트/별점/이미지 수정
 */
public class TemplateUpdateRequest {
    private Long templateId;
    private String text;
    private int star;
    private List<String> imageUrls;

    // Getter, Setter
    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
