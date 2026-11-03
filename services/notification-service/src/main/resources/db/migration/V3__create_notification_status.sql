CREATE TABLE notification_status (
    id BIGSERIAL PRIMARY KEY,

    message_id VARCHAR(255) NOT NULL UNIQUE,

    order_id BIGINT NOT NULL,

    channel VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL,

    recipient VARCHAR(255) NOT NULL,

    failure_reason VARCHAR(1000),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_notification_status_message_id
    ON notification_status (message_id);

CREATE INDEX idx_notification_status_order_id
    ON notification_status (order_id);

CREATE INDEX idx_notification_status_order_channel
    ON notification_status (order_id, channel);