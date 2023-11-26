CREATE TABLE IF NOT EXISTS notification(
    id UUID PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL,
    title VARCHAR(1000) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    event_at TIMESTAMP(6) NOT NULL,
    notify_at TIMESTAMP(6) NOT NULL,
    completed BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS task(
    id VARCHAR(64) PRIMARY KEY,
    is_completed BOOLEAN NOT NULL,
    is_recurring BOOLEAN,
    content VARCHAR(1000) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    date TIMESTAMP(6),
    datetime TIMESTAMP(6),
    string VARCHAR(25),
    timezone VARCHAR(25),
    url VARCHAR(64) NOT NULL,
    is_processed BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS task_label(
    task_id VARCHAR(64) NOT NULL,
    label VARCHAR(64) NOT NULL
);