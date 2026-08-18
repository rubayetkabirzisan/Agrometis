-- V3: Farm Profiles table
CREATE TABLE farm_profiles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE,
    farm_name   VARCHAR(120),
    location    VARCHAR(200),
    soil_type   VARCHAR(80),
    area_hectares FLOAT,
    primary_crops VARCHAR(300),
    region      VARCHAR(100),
    last_updated TIMESTAMP NOT NULL,
    CONSTRAINT fk_farm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
