package com.yosmerry.pims.inventory.controller;

import com.yosmerry.pims.common.constant.BasePathNames;
import com.yosmerry.pims.common.request.RequestHeaders;
import com.yosmerry.pims.common.response.ApiResponse;
import com.yosmerry.pims.common.response.ResponseUtils;
import com.yosmerry.pims.inventory.dto.CreateInventoryItemRequest;
import com.yosmerry.pims.inventory.dto.InventoryItemFilter;
import com.yosmerry.pims.inventory.dto.InventoryItemResponse;
import com.yosmerry.pims.inventory.dto.UpdateInventoryItemRequest;
import com.yosmerry.pims.inventory.service.InventoryService;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(BasePathNames.INVENTORY_ITEMS)
@Tag(name = "Inventory", description = "Personal inventory item APIs")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

  private final InventoryService inventoryService;

  @PostMapping
  @Operation(description = "Create an inventory item")
  public ResponseEntity<ApiResponse<InventoryItemResponse>> create(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @Valid @RequestBody CreateInventoryItemRequest request) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    InventoryItemResponse response = inventoryService.create(request);

    return ResponseUtils.created(response, requestHeaders.requestId());
  }

  @GetMapping
  @Operation(description = "Search and filter inventory items owned by the current user")
  public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> findAll(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @Valid @ModelAttribute InventoryItemFilter filter) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    List<InventoryItemResponse> response = inventoryService.findAll(filter);

    return ResponseUtils.ok(response, requestHeaders.requestId());
  }

  @GetMapping("/{code}")
  @Operation(description = "Get an inventory item by code")
  public ResponseEntity<ApiResponse<InventoryItemResponse>> findByCode(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String code) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    InventoryItemResponse response = inventoryService.findByCode(code);

    return ResponseUtils.ok(response, requestHeaders.requestId());
  }

  @PutMapping("/{code}")
  @Operation(description = "Update an inventory item")
  public ResponseEntity<ApiResponse<InventoryItemResponse>> update(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String code,
      @Valid @RequestBody UpdateInventoryItemRequest request) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    InventoryItemResponse response = inventoryService.update(code, request);

    return ResponseUtils.ok(response, requestHeaders.requestId());
  }

  @DeleteMapping("/{code}")
  @Operation(description = "Delete an inventory item")
  public ResponseEntity<ApiResponse<Void>> delete(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String code) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    inventoryService.delete(code);

    return ResponseUtils.ok(null, requestHeaders.requestId());
  }
}
