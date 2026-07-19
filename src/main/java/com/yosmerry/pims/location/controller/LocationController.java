package com.yosmerry.pims.location.controller;

import com.yosmerry.pims.location.dto.LocationResponse;
import com.yosmerry.pims.location.dto.CreateLocationRequest;
import com.yosmerry.pims.location.dto.UpdateLocationRequest;
import com.yosmerry.pims.location.service.LocationService;
import com.yosmerry.pims.common.constant.BasePathNames;
import com.yosmerry.pims.common.request.RequestHeaders;
import com.yosmerry.pims.common.request.PagingRequest;
import com.yosmerry.pims.common.response.ApiResponse;
import com.yosmerry.pims.common.response.PageResponse;
import com.yosmerry.pims.common.response.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(BasePathNames.LOCATIONS)
@Tag(name = "Locations", description = "Personal inventory location APIs")
@SecurityRequirement(name = "bearerAuth")
public class LocationController {

  private final LocationService locationService;

  @PostMapping
  @Operation(description = "Create a location")
  public ResponseEntity<ApiResponse<LocationResponse>> create(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @Valid @RequestBody CreateLocationRequest request) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    LocationResponse response = locationService.create(request);

    return ResponseUtils.created(response, requestHeaders.requestId());
  }

  @GetMapping
  @Operation(description = "Get locations owned by the current user")
  public ResponseEntity<ApiResponse<PageResponse<LocationResponse>>> findAll(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @Valid @ModelAttribute PagingRequest pagingRequest) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    PageResponse<LocationResponse> response = locationService.findAll(pagingRequest);

    return ResponseUtils.ok(response, requestHeaders.requestId());
  }

  @GetMapping("/{code}")
  @Operation(description = "Get a location by code")
  public ResponseEntity<ApiResponse<LocationResponse>> findByCode(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String code) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    LocationResponse response = locationService.findByCode(code);

    return ResponseUtils.ok(response, requestHeaders.requestId());
  }

  @PutMapping("/{code}")
  @Operation(description = "Update a location")
  public ResponseEntity<ApiResponse<LocationResponse>> update(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String code,
      @Valid @RequestBody UpdateLocationRequest request) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    LocationResponse response = locationService.update(code, request);

    return ResponseUtils.ok(response, requestHeaders.requestId());
  }

  @DeleteMapping("/{code}")
  @Operation(description = "Delete a location")
  public ResponseEntity<ApiResponse<Void>> delete(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String code) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    locationService.delete(code);

    return ResponseUtils.ok(null, requestHeaders.requestId());
  }
}
