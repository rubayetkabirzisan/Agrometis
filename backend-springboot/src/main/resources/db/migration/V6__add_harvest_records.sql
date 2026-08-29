-- V6: Historical Harvest Records
CREATE TABLE harvest_records (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    crop_name       VARCHAR(120) NOT NULL,
    season          VARCHAR(60),
    harvest_year    INT NOT NULL,
    actual_yield_kg DOUBLE NOT NULL,
    revenue_usd     DOUBLE DEFAULT 0.0,
    notes           VARCHAR(500),
    recorded_at     TIMESTAMP NOT NULL,
    CONSTRAINT fk_harvest_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
