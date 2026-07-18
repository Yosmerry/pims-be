-- =========================================================
-- PUBLIC CODE SEQUENCES
-- =========================================================

CREATE SEQUENCE IF NOT EXISTS users_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS refresh_tokens_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS categories_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS locations_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS inventory_items_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS item_images_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS inventory_activity_records_code_seq START WITH 1 INCREMENT BY 1;

-- =========================================================
-- USERS
-- =========================================================

CREATE TABLE IF NOT EXISTS users (
    id                  BIGINT          PRIMARY KEY,
    code                VARCHAR(20)     NOT NULL
        DEFAULT ('USR' || LPAD(nextval('users_code_seq')::TEXT, 6, '0')),
    email               VARCHAR(255)    NOT NULL,
    password_hash       VARCHAR(255)    NOT NULL,
    name                VARCHAR(150)    NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    last_login_date     BIGINT,

    created_date        BIGINT          NOT NULL,
    created_by          VARCHAR(100)    NOT NULL,
    updated_date        BIGINT          NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    version             BIGINT          NOT NULL DEFAULT 0,
    mark_for_delete     BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_users_code UNIQUE (code),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_lower
    ON users (LOWER(email));

CREATE INDEX IF NOT EXISTS idx_users_status
    ON users (status, mark_for_delete);

-- =========================================================
-- REFRESH TOKENS
-- =========================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id                  BIGINT          PRIMARY KEY,
    code                VARCHAR(20)     NOT NULL
        DEFAULT ('RFT' || LPAD(nextval('refresh_tokens_code_seq')::TEXT, 6, '0')),
    user_code           VARCHAR(20)     NOT NULL,
    token_hash          VARCHAR(255)    NOT NULL,
    expires_date        BIGINT          NOT NULL,
    revoked_date        BIGINT,
    replaced_by_code    VARCHAR(20),
    device_info         VARCHAR(255),

    created_date        BIGINT          NOT NULL,
    created_by          VARCHAR(100)    NOT NULL,
    updated_date        BIGINT          NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    version             BIGINT          NOT NULL DEFAULT 0,
    mark_for_delete     BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_refresh_tokens_code UNIQUE (code),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash)
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_code
    ON refresh_tokens (user_code, mark_for_delete);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_active
    ON refresh_tokens (user_code, expires_date)
    WHERE revoked_date IS NULL AND mark_for_delete = FALSE;

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_replaced_by_code
    ON refresh_tokens (replaced_by_code)
    WHERE replaced_by_code IS NOT NULL;

-- =========================================================
-- CATEGORIES
-- =========================================================

CREATE TABLE IF NOT EXISTS categories (
    id                  BIGINT          PRIMARY KEY,
    code                VARCHAR(20)     NOT NULL
        DEFAULT ('CAT' || LPAD(nextval('categories_code_seq')::TEXT, 6, '0')),
    user_code           VARCHAR(20)     NOT NULL,
    name                VARCHAR(100)    NOT NULL,
    description         VARCHAR(500),
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',

    created_date        BIGINT          NOT NULL,
    created_by          VARCHAR(100)    NOT NULL,
    updated_date        BIGINT          NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    version             BIGINT          NOT NULL DEFAULT 0,
    mark_for_delete     BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_categories_code UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_categories_user_code
    ON categories (user_code, mark_for_delete);

CREATE INDEX IF NOT EXISTS idx_categories_user_name
    ON categories (user_code, LOWER(name), mark_for_delete);

-- =========================================================
-- LOCATIONS
-- =========================================================

CREATE TABLE IF NOT EXISTS locations (
    id                  BIGINT          PRIMARY KEY,
    code                VARCHAR(20)     NOT NULL
        DEFAULT ('LOC' || LPAD(nextval('locations_code_seq')::TEXT, 6, '0')),
    user_code           VARCHAR(20)     NOT NULL,
    name                VARCHAR(100)    NOT NULL,
    description         VARCHAR(500),
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',

    created_date        BIGINT          NOT NULL,
    created_by          VARCHAR(100)    NOT NULL,
    updated_date        BIGINT          NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    version             BIGINT          NOT NULL DEFAULT 0,
    mark_for_delete     BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_locations_code UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_locations_user_code
    ON locations (user_code, mark_for_delete);

CREATE INDEX IF NOT EXISTS idx_locations_user_name
    ON locations (user_code, LOWER(name), mark_for_delete);

-- =========================================================
-- INVENTORY ITEMS
-- =========================================================

CREATE TABLE IF NOT EXISTS inventory_items (
    id                  BIGINT          PRIMARY KEY,
    code                VARCHAR(20)     NOT NULL
        DEFAULT ('ITM' || LPAD(nextval('inventory_items_code_seq')::TEXT, 6, '0')),
    user_code           VARCHAR(20)     NOT NULL,
    category_code       VARCHAR(20)     NOT NULL,
    location_code       VARCHAR(20),
    name                VARCHAR(150)    NOT NULL,
    description         VARCHAR(1000),
    quantity            INTEGER         NOT NULL DEFAULT 1,
    purchase_price      NUMERIC(19, 2),
    purchase_date       DATE,
    condition           VARCHAR(20)     NOT NULL DEFAULT 'GOOD',
    status              VARCHAR(20)     NOT NULL DEFAULT 'OWNED',
    notes               VARCHAR(1000),

    created_date        BIGINT          NOT NULL,
    created_by          VARCHAR(100)    NOT NULL,
    updated_date        BIGINT          NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    version             BIGINT          NOT NULL DEFAULT 0,
    mark_for_delete     BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_inventory_items_code UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_inventory_items_user_code
    ON inventory_items (user_code, mark_for_delete);

CREATE INDEX IF NOT EXISTS idx_inventory_items_category_code
    ON inventory_items (user_code, category_code, mark_for_delete);

CREATE INDEX IF NOT EXISTS idx_inventory_items_location_code
    ON inventory_items (user_code, location_code, mark_for_delete);

CREATE INDEX IF NOT EXISTS idx_inventory_items_status
    ON inventory_items (user_code, status, mark_for_delete);

CREATE INDEX IF NOT EXISTS idx_inventory_items_condition
    ON inventory_items (user_code, condition, mark_for_delete);

CREATE INDEX IF NOT EXISTS idx_inventory_items_name
    ON inventory_items (user_code, LOWER(name), mark_for_delete);

CREATE INDEX IF NOT EXISTS idx_inventory_items_purchase_date
    ON inventory_items (user_code, purchase_date DESC, mark_for_delete);

-- =========================================================
-- ITEM IMAGES
-- =========================================================

CREATE TABLE IF NOT EXISTS item_images (
    id                      BIGINT          PRIMARY KEY,
    code                    VARCHAR(20)     NOT NULL
        DEFAULT ('IMG' || LPAD(nextval('item_images_code_seq')::TEXT, 6, '0')),
    inventory_item_code     VARCHAR(20)     NOT NULL,
    original_filename       VARCHAR(255)    NOT NULL,
    stored_filename         VARCHAR(255)    NOT NULL,
    content_type            VARCHAR(100)    NOT NULL,
    file_size               BIGINT          NOT NULL,
    storage_path            VARCHAR(500)    NOT NULL,
    is_primary              BOOLEAN         NOT NULL DEFAULT FALSE,

    created_date            BIGINT          NOT NULL,
    created_by              VARCHAR(100)    NOT NULL,
    updated_date            BIGINT          NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    version                 BIGINT          NOT NULL DEFAULT 0,
    mark_for_delete         BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_item_images_code UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_item_images_inventory_item_code
    ON item_images (inventory_item_code, mark_for_delete);

CREATE INDEX IF NOT EXISTS idx_item_images_primary
    ON item_images (inventory_item_code, is_primary, mark_for_delete);

-- =========================================================
-- INVENTORY ACTIVITY RECORDS
-- =========================================================

CREATE TABLE IF NOT EXISTS inventory_activity_records (
    id                      BIGINT          PRIMARY KEY,
    code                    VARCHAR(20)     NOT NULL
        DEFAULT ('ACT' || LPAD(nextval('inventory_activity_records_code_seq')::TEXT, 6, '0')),
    user_code               VARCHAR(20)     NOT NULL,
    inventory_item_code     VARCHAR(20)     NOT NULL,
    action                  VARCHAR(30)     NOT NULL,
    reason                  VARCHAR(500),
    changes                 JSONB,

    created_date            BIGINT          NOT NULL,
    created_by              VARCHAR(100)    NOT NULL,
    updated_date            BIGINT          NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    version                 BIGINT          NOT NULL DEFAULT 0,
    mark_for_delete         BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_inventory_activity_records_code UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_inventory_activity_records_user_code
    ON inventory_activity_records (user_code, created_date DESC);

CREATE INDEX IF NOT EXISTS idx_inventory_activity_records_item_code
    ON inventory_activity_records (inventory_item_code, created_date DESC);

CREATE INDEX IF NOT EXISTS idx_inventory_activity_records_action
    ON inventory_activity_records (action, created_date DESC);
