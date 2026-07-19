package com.yosmerry.pims.inventory.dto;

import com.yosmerry.pims.common.constant.ErrorCodes;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInventoryItemRequest(

    @NotBlank(message = ErrorCodes.BLANK)
    @Size(max = 20, message = ErrorCodes.CHARACTER_MORE_THAN_20)
    String categoryCode,

    @Size(max = 20, message = ErrorCodes.CHARACTER_MORE_THAN_20)
    String locationCode,

    @NotBlank(message = ErrorCodes.BLANK)
    @Size(max = 150, message = ErrorCodes.CHARACTER_MORE_THAN_150)
    String name,

    @Size(max = 1000, message = ErrorCodes.CHARACTER_MORE_THAN_1000)
    String description,

    @NotNull(message = ErrorCodes.BLANK)
    @Min(value = 1, message = ErrorCodes.MINIMUM_1)
    Integer quantity,

    @DecimalMin(value = "0.00", message = ErrorCodes.MINIMUM_0)
    @Digits(integer = 17, fraction = 2, message = ErrorCodes.INVALID_FORMAT)
    BigDecimal purchasePrice,

    @PastOrPresent(message = ErrorCodes.FUTURE_DATE)
    LocalDate purchaseDate,

    @NotBlank(message = ErrorCodes.BLANK)
    @Pattern(
        regexp = "NEW|GOOD|FAIR|POOR|DAMAGED",
        message = ErrorCodes.INVALID)
    String condition,

    @Size(max = 1000, message = ErrorCodes.CHARACTER_MORE_THAN_1000)
    String notes) {
}
