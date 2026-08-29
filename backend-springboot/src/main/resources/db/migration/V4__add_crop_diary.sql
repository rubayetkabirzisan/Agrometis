-- V4: Crop Diary tables
CREATE TABLE crop_cycles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    crop_name   VARCHAR(120) NOT NULL,
    season      VARCHAR(60),
    start_date  DATE NOT NULL,
    status      VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    notes       VARCHAR(500),
    created_at  TIMESTAMP NOT NULL,
    CONSTRAINT fk_cycle_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE activity_logs (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    cycle_id      BIGINT NOT NULL,
    activity_type VARCHAR(60) NOT NULL,
    description   VARCHAR(500),
    cost          DOUBLE DEFAULT 0.0,
    logged_at     TIMESTAMP NOT NULL,
    CONSTRAINT fk_activity_cycle FOREIGN KEY (cycle_id) REFERENCES crop_cycles(id) ON DELETE CASCADE
);
