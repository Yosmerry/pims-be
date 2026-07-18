package com.yosmerry.pims.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorCodes {

  public static final String BLANK = "Blank";
  public static final String DUPLICATE = "Duplicate";
  public static final String CHARACTER_MORE_THAN_150 = "CharacterMoreThan150";
  public static final String CHARACTER_LESS_THAN_8 = "CharacterLessThan8";
  public static final String CHARACTER_MORE_THAN_72 = "CharacterMoreThan72";
  public static final String CHARACTER_MORE_THAN_255 = "CharacterMoreThan255";
  public static final String INVALID_FORMAT = "InvalidFormat";
  public static final String WEAK_PASSWORD = "WeakPassword";
  public static final String PASSWORD_MISMATCH = "PasswordMismatch";
  public static final String INVALID_CREDENTIALS = "InvalidCredentials";
  public static final String USER_INACTIVE = "UserInactive";
  public static final String MISSING = "Missing";
  public static final String INVALID = "Invalid";

}
