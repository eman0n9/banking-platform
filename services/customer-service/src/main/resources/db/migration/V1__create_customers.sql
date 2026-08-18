CREATE TABLE customers
(
    id         UUID         NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(254) NOT NULL,
    phone      VARCHAR(32),
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version    BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT pk_customers PRIMARY KEY (id),

    CONSTRAINT chk_customers_first_name_not_blank
        CHECK (btrim(first_name) <> ''),

    CONSTRAINT chk_customers_last_name_not_blank
        CHECK (btrim(last_name) <> ''),

    CONSTRAINT chk_customers_email_not_blank
        CHECK (btrim(email) <> ''),

    CONSTRAINT chk_customers_phone_not_blank
        CHECK (phone IS NULL OR btrim(phone) <> ''),

    CONSTRAINT chk_customers_status
        CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED'))
);

CREATE UNIQUE INDEX ux_customers_email_lower
    ON customers (lower(email));