package com.yosmerry.pims.common.request;

import com.yosmerry.pims.common.constant.ErrorCodes;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagingRequest {

  @NotNull(message = ErrorCodes.BLANK)
  @Min(value = 0, message = ErrorCodes.MINIMUM_0)
  private Integer page = 0;

  @NotNull(message = ErrorCodes.BLANK)
  @Min(value = 1, message = ErrorCodes.MINIMUM_1)
  @Max(value = 100, message = ErrorCodes.MAXIMUM_100)
  private Integer size = 20;
}
