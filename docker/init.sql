CREATE TABLE IF NOT EXISTS notification(
    id UUID PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    timestamp TIMESTAMP(6) NOT NULL,
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
    url VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS task_label(
    task_id VARCHAR(64) NOT NULL,
    label VARCHAR(64) NOT NULL
);