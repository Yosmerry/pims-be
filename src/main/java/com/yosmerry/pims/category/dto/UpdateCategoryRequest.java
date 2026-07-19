package com.yosmerry.pims.category.dto;

import com.yosmerry.pims.common.constant.ErrorCodes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(

    @NotBlank(message = ErrorCodes.BLANK)
    @Size(max = 100, message = ErrorCodes.CHARACTER_MORE_THAN_100)
    String name,

    @Size(max = 500, message = ErrorCodes.CHARACTER_MORE_THAN_500)
    String description,

    @NotBlank(message = ErrorCodes.BLANK)
    @Pattern(regexp = "ACTIVE|INACTIVE", message = ErrorCodes.INVALID)
    String status) {
}
