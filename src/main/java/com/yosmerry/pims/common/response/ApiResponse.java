package com.yosmerry.pims.common.response;

public record ApiResponse<T>(
    T data,
        Metadata metadata) {
}
