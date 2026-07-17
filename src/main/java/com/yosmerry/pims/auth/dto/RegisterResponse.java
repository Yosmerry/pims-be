package com.yosmerry.pims.auth.dto;

public record RegisterResponse(
        String code,
        String name,
        String email,
        String status,
        Long createdDate,
        Long updatedDate
) {
}
