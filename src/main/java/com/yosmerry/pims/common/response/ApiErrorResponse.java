package com.yosmerry.pims.common.response;

import java.util.List;
import java.util.Map;

public record ApiErrorResponse(
        int code,
        String status,
        Map<String, List<String>> errors,
        Metadata metadata
) {
}
