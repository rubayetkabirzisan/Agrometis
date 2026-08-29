-- V5: Smart Alerts table
CREATE TABLE alerts (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    type        VARCHAR(60) NOT NULL,
    message     VARCHAR(500) NOT NULL,
    severity    VARCHAR(20) NOT NULL DEFAULT 'INFO',
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL,
    CONSTRAINT fk_alert_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
