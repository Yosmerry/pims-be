package com.yosmerry.pims.image.controller;

import com.yosmerry.pims.common.constant.BasePathNames;
import com.yosmerry.pims.common.request.RequestHeaders;
import com.yosmerry.pims.common.response.ApiResponse;
import com.yosmerry.pims.common.response.ResponseUtils;
import com.yosmerry.pims.image.dto.ImageResponse;
import com.yosmerry.pims.image.model.ImageContent;
import com.yosmerry.pims.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Tag(name = "Images", description = "Inventory item image APIs")
@SecurityRequirement(name = "bearerAuth")
public class ImageController {

  private final ImageService imageService;

  @PostMapping(
      path = BasePathNames.INVENTORY_ITEMS + "/{inventoryItemCode}/images",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(description = "Upload an image for an inventory item")
  public ResponseEntity<ApiResponse<ImageResponse>> upload(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String inventoryItemCode,
      @RequestPart("file") MultipartFile file) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    ImageResponse response = imageService.upload(inventoryItemCode, file);

    return ResponseUtils.created(response, requestHeaders.requestId());
  }

  @GetMapping(BasePathNames.IMAGES + "/{imageCode}")
  @Operation(description = "Get image content")
  public ResponseEntity<Resource> findContent(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @PathVariable String imageCode) {
    RequestHeaders.from(httpHeaders);
    ImageContent imageContent = imageService.findContent(imageCode);
    ContentDisposition contentDisposition = ContentDisposition.inline()
        .filename(imageContent.originalFilename(), StandardCharsets.UTF_8)
        .build();

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(imageContent.contentType()))
        .contentLength(imageContent.fileSize())
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        .body(imageContent.resource());
  }
}
