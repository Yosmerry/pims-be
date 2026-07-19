package com.yosmerry.pims.location.dto;

import lombok.Builder;

@Builder
public record LocationResponse(
    String code,
    String name,
    String description,
    String status,
    Long createdDate,
    Long updatedDate) {
}
