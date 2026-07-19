package com.yosmerry.pims.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableNames {
  public static final String USERS = "users";
  public static final String REFRESH_TOKENS = "refresh_tokens";
  public static final String CATEGORIES = "categories";
  public static final String LOCATIONS = "locations";
  public static final String INVENTORY_ITEMS = "inventory_items";
  public static final String ITEM_IMAGES = "item_images";
}
