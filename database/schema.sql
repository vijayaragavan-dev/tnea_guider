CREATE DATABASE IF NOT EXISTS tnea_guider;
USE tnea_guider;

CREATE TABLE colleges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    district VARCHAR(100) NOT NULL,
    tier ENUM('Tier 1','Tier 2','Tier 3') NOT NULL,
    branch VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    cutoff DECIMAL(5,2) NOT NULL,
    fees INT NOT NULL,
    placement_rate INT NOT NULL,

    CHECK (cutoff BETWEEN 100 AND 200),
    CHECK (fees BETWEEN 50000 AND 200000),
    CHECK (placement_rate BETWEEN 40 AND 100)
);

CREATE INDEX idx_cutoff ON colleges(cutoff);
CREATE INDEX idx_district ON colleges(district);
CREATE INDEX idx_tier ON colleges(tier);
CREATE INDEX idx_branch ON colleges(branch);
