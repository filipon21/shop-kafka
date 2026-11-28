CREATE TABLE outbox_events
(
    id            UUID PRIMARY KEY,
    aggregate_id  BIGINT                   NOT NULL,
    event_type    VARCHAR(100)             NOT NULL,
    topic         VARCHAR(255)             NOT NULL,
    message_key   VARCHAR(255)             NOT NULL,
    payload       TEXT                     NOT NULL,

    status        VARCHAR(20)              NOT NULL DEFAULT 'PENDING',
    retry_count   INTEGER                  NOT NULL DEFAULT 0,

    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at  TIMESTAMP WITH TIME ZONE,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    last_error    TEXT
);

CREATE INDEX idx_outbox_pending
    ON outbox_events (status, next_retry_at, created_at);