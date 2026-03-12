CREATE TABLE IF NOT EXISTS upload (
    upload_id    CHAR(36)     NOT NULL,
    s3_upload_id VARCHAR(255) NOT NULL,
    file_key     VARCHAR(500) NOT NULL,
    user_id      VARCHAR(255) NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    PRIMARY KEY (upload_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
