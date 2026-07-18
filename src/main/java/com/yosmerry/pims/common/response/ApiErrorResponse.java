package com.yosmerry.pims.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
    int code,
    Map<String, List<String>> errors,
    Metadata metadata) {
}
