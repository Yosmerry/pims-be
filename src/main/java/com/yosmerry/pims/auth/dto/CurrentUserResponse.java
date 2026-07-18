package com.yosmerry.pims.auth.dto;

import lombok.Builder;

@Builder
public record CurrentUserResponse(
    String code,
    String name,
    String email,
    String status,
    Long lastLoginDate,
    Long createdDate,
    Long updatedDate) {
}
