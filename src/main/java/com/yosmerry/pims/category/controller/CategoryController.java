package com.yosmerry.pims.category.controller;

import com.yosmerry.pims.category.dto.CategoryResponse;
import com.yosmerry.pims.category.dto.CreateCategoryRequest;
import com.yosmerry.pims.category.dto.UpdateCategoryRequest;
import com.yosmerry.pims.category.service.CategoryService;
import com.yosmerry.pims.common.constant.BasePathNames;
import com.yosmerry.pims.common.request.RequestHeaders;
import com.yosmerry.pims.common.response.ApiResponse;
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
@RequestMapping(BasePathNames.CATEGORIES)
@Tag(name = "Categories", description = "Personal inventory category APIs")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

  private final CategoryService categoryService;

  @PostMapping
  @Operation(description = "Create a category")
  public ResponseEntity<ApiResponse<CategoryResponse>> create(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @Valid @RequestBody CreateCategoryRequest request) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    CategoryResponse response = categoryService.create(request);

    return ResponseUtils.created(response, requestHeaders.requestId());
  }

  @GetMapping
  @Operation(description = "Get all categories owned by the current user")
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> findAll(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    List<CategoryResponse> response = categoryService.findAll();

    return ResponseUtils.ok(response, requestHeaders.requestId());
  }

  @GetMapping("/{code}")
  @Operation(description = "Get a category by code")
  public ResponseEntity<ApiResponse<CategoryResponse>> findByCode(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String code) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    CategoryResponse response = categoryService.findByCode(code);

    return ResponseUtils.ok(response, requestHeaders.requestId());
  }

  @PutMapping("/{code}")
  @Operation(description = "Update a category")
  public ResponseEntity<ApiResponse<CategoryResponse>> update(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String code,
      @Valid @RequestBody UpdateCategoryRequest request) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    CategoryResponse response = categoryService.update(code, request);

    return ResponseUtils.ok(response, requestHeaders.requestId());
  }

  @DeleteMapping("/{code}")
  @Operation(description = "Delete a category")
  public ResponseEntity<ApiResponse<Void>> delete(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String code) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    categoryService.delete(code);

    return ResponseUtils.ok(null, requestHeaders.requestId());
  }
}
