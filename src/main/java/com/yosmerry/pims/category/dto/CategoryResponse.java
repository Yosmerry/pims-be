package com.yosmerry.pims.category.dto;

import lombok.Builder;

@Builder
public record CategoryResponse(
    String code,
    String name,
    String description,
    String status,
    Long createdDate,
    Long updatedDate) {
}
