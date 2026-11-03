CREATE TABLE processed_messages (
    message_id VARCHAR(100) PRIMARY KEY,
    message_type VARCHAR(100) NOT NULL,
    source VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP NOT NULL
);