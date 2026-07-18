package com.yosmerry.pims.auth.dto;

import lombok.Builder;

@Builder
public record RegisterResponse(
    String code,
    String name,
    String email,
    String status,
    Long createdDate,
    Long updatedDate) {
}
