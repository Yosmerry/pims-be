package com.yosmerry.pims.common.response;

public record ApiResponse<T>(
        int code,
        String status,
        T data,
        Metadata metadata
) {
    public static <T> ApiResponse<T> created(T data, String requestId) {
        return new ApiResponse<>(201, "CREATED", data, new Metadata(requestId));
    }
}
