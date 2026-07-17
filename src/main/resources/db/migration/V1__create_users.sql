CREATE SEQUENCE IF NOT EXISTS users_code_seq
    START WITH 1
    INCREMENT BY 1;

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
    CONSTRAINT uk_users_code UNIQUE (code)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_lower
    ON users (LOWER(email));

CREATE INDEX IF NOT EXISTS idx_users_status
    ON users (status, mark_for_delete);
