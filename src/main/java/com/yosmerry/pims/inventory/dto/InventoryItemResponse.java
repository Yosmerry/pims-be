package com.yosmerry.pims.inventory.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record InventoryItemResponse(
    String code,
    String categoryCode,
    String locationCode,
    String name,
    String description,
    Integer quantity,
    BigDecimal purchasePrice,
    LocalDate purchaseDate,
    String condition,
    String status,
    String notes,
    Long createdDate,
    Long updatedDate) {
}
