package com.yosmerry.pims.image.dto;

import lombok.Builder;

@Builder
public record ImageResponse(
    String code,
    String inventoryItemCode,
    String originalFilename,
    String contentType,
    Long fileSize,
    Boolean primary,
    String url,
    Long createdDate) {
}
