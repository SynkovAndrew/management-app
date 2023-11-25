CREATE TABLE IF NOT EXIST notification_task(
    id UUID PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    timestamp TIMESTAMP(6) NOT NULL,
    completed BOOLEAN NOT NULL
);