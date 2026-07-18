package com.yosmerry.pims.common.request;

import com.yosmerry.pims.common.exception.ApiValidationException;
import org.springframework.http.HttpHeaders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RequestHeaders(
    String channelId,
    String serviceId,
    String requestId) {

  private static final String CHANNEL_ID = "X-CHANNEL-ID";
  private static final String SERVICE_ID = "X-SERVICE-ID";
  private static final String REQUEST_ID = "X-REQUEST-ID";

  public static RequestHeaders from(HttpHeaders headers) {
    String channelId = headers.getFirst(CHANNEL_ID);
    String serviceId = headers.getFirst(SERVICE_ID);
    String requestId = headers.getFirst(REQUEST_ID);

    validate(channelId, serviceId);

    if (requestId == null || requestId.isBlank()) {
      requestId = UUID.randomUUID().toString();
    }

    return new RequestHeaders(channelId, serviceId, requestId);
  }

  private static void validate(String channelId, String serviceId) {
    Map<String, List<String>> errors = new LinkedHashMap<>();

    addBlankError(errors, "channelId", channelId);
    addBlankError(errors, "serviceId", serviceId);

    if (!errors.isEmpty()) {
      throw new ApiValidationException(errors);
    }
  }

  private static void addBlankError(
      Map<String, List<String>> errors,
      String field,
      String value) {
    if (value == null || value.isBlank()) {
      errors.put(field, List.of("Blank"));
    }
  }
}