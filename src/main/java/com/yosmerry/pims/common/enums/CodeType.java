package com.yosmerry.pims.common.enums;

public enum CodeType {
    USER("users_code_seq", "USR"),
    REFRESH_TOKEN("refresh_tokens_code_seq", "RFT"),
    CATEGORY("categories_code_seq", "CAT"),
    LOCATION("locations_code_seq", "LOC"),
    INVENTORY_ITEM("inventory_items_code_seq", "ITM"),
    ITEM_IMAGE("item_images_code_seq", "IMG"),
    INVENTORY_ACTIVITY_RECORD("inventory_activity_records_code_seq", "ACT");

    private final String sequenceName;
    private final String prefix;

    CodeType(String sequenceName, String prefix) {
        this.sequenceName = sequenceName;
        this.prefix = prefix;
    }

    public String sequenceName() {
        return sequenceName;
    }

    public String prefix() {
        return prefix;
    }
}
