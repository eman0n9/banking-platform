CREATE TABLE outbox_events
(
    event_id       UUID PRIMARY KEY,
    aggregate_type VARCHAR(100)             NOT NULL,
    aggregate_id   UUID                     NOT NULL,
    event_type     VARCHAR(150)             NOT NULL,
    topic          VARCHAR(200)             NOT NULL,
    event_key      VARCHAR(200)             NOT NULL,
    payload        TEXT                     NOT NULL,
    created_at     TIMESTAMPTZ              NOT NULL,
    published_at   TIMESTAMPTZ,
    attempts       INTEGER                  NOT NULL DEFAULT 0,
    last_error     TEXT,

    CONSTRAINT chk_outbox_attempts_non_negative
        CHECK (attempts >= 0)
);

CREATE INDEX idx_outbox_events_unpublished
    ON outbox_events (created_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events (aggregate_type, aggregate_id);