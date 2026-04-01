CREATE DATABASE IF NOT EXISTS tnea_guider;
USE tnea_guider;

DROP TABLE IF EXISTS cutoff_data;
DROP TABLE IF EXISTS colleges;
DROP TABLE IF EXISTS branches;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS districts;

CREATE TABLE districts (
<<<<<<< Updated upstream
    id INT AUTO_INCREMENT PRIMARY KEY,
=======
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
>>>>>>> Stashed changes
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE colleges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
<<<<<<< Updated upstream
    district_id INT NOT NULL,
=======
    district_id BIGINT NOT NULL,
>>>>>>> Stashed changes
    tier ENUM('Tier 1','Tier 2','Tier 3') NOT NULL,
    FOREIGN KEY (district_id) REFERENCES districts(id),
    UNIQUE KEY unique_college (name, district_id, tier)
);

CREATE TABLE branches (
<<<<<<< Updated upstream
    id INT AUTO_INCREMENT PRIMARY KEY,
=======
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
>>>>>>> Stashed changes
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE categories (
<<<<<<< Updated upstream
    id INT AUTO_INCREMENT PRIMARY KEY,
=======
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
>>>>>>> Stashed changes
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE cutoff_data (
<<<<<<< Updated upstream
    id INT AUTO_INCREMENT PRIMARY KEY,
    college_id INT NOT NULL,
    branch_id INT NOT NULL,
    category_id INT NOT NULL,
=======
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    college_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
>>>>>>> Stashed changes
    cutoff DECIMAL(5,2) NOT NULL,
    fees INT NOT NULL,
    placement_rate INT NOT NULL,
    FOREIGN KEY (college_id) REFERENCES colleges(id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES branches(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    UNIQUE KEY unique_cutoff (college_id, branch_id, category_id)
);

CREATE INDEX idx_cutoff ON cutoff_data(cutoff);
CREATE INDEX idx_fees ON cutoff_data(fees);
CREATE INDEX idx_placement ON cutoff_data(placement_rate);
CREATE INDEX idx_college ON cutoff_data(college_id);
CREATE INDEX idx_branch ON cutoff_data(branch_id);
CREATE INDEX idx_category ON cutoff_data(category_id);
CREATE INDEX idx_district ON colleges(district_id);