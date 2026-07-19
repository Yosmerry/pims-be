package com.yosmerry.pims.inventory.dto;

import com.yosmerry.pims.common.constant.ErrorCodes;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryItemFilter {

  @Size(max = 150, message = ErrorCodes.CHARACTER_MORE_THAN_150)
  private String search;

  @Size(max = 20, message = ErrorCodes.CHARACTER_MORE_THAN_20)
  private String categoryCode;

  @Size(max = 20, message = ErrorCodes.CHARACTER_MORE_THAN_20)
  private String locationCode;

  @Pattern(
      regexp = "NEW|GOOD|FAIR|POOR|DAMAGED",
      message = ErrorCodes.INVALID)
  private String condition;

  @Pattern(
      regexp = "OWNED|LOANED|SOLD|LOST|DISPOSED",
      message = ErrorCodes.INVALID)
  private String status;

  @Pattern(
      regexp = "(name|updatedDate|purchaseDate):(asc|desc)",
      message = ErrorCodes.INVALID)
  private String sortBy = "updatedDate:desc";
}
