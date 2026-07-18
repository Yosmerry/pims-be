package com.yosmerry.pims.common.response;

public record ApiResponse<T>(
    int code,
    T data,
    Metadata metadata) {
}
