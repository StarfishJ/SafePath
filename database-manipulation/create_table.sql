-- //////////////////////////////////////////////////////
-- Global Settings
-- //////////////////////////////////////////////////////
CREATE DATABASE IF NOT EXISTS safepath;

ALTER DATABASE safepath
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

USE safepath;  # Select the current database

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET COLLATION_CONNECTION = 'utf8mb4_unicode_ci';
SET default_storage_engine=INNODB;


-- //////////////////////////////////////////////////////
-- Delete from the table with the strongest foreign key dependency first
-- //////////////////////////////////////////////////////
DROP TABLE IF EXISTS user_alerts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS street_segments;
DROP TABLE IF EXISTS intersections;
DROP TABLE IF EXISTS realtime_incidents;
DROP TABLE IF EXISTS report_offenses;
DROP TABLE IF EXISTS offense_types;
DROP TABLE IF EXISTS crime_reports;
DROP TABLE IF EXISTS etl_runs;
DROP TABLE IF EXISTS sources;

-- //////////////////////////////////////////////////////
-- App Internal Data
-- //////////////////////////////////////////////////////
CREATE TABLE sources (
    source_id INT AUTO_INCREMENT PRIMARY KEY,
    source_name VARCHAR(255) NOT NULL,
    url VARCHAR(255),
    refresh_interval INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE etl_runs (
    etl_id INT AUTO_INCREMENT PRIMARY KEY,
    source_id INT NOT NULL,
    run_time DATETIME NOT NULL,
    record_count INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id) REFERENCES sources(source_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- //////////////////////////////////////////////////////
-- SPD Crime Data
-- //////////////////////////////////////////////////////
CREATE TABLE crime_reports (
    report_number VARCHAR(50) PRIMARY KEY,
    report_datetime DATETIME,
    precinct VARCHAR(100),
    sector VARCHAR(10),
    beat VARCHAR(10),
    mcpp_neighborhood VARCHAR(100),
    blurred_address VARCHAR(255),
    blurred_latitude DOUBLE,
    blurred_longitude DOUBLE,
    etl_id INT,
    FOREIGN KEY (etl_id) REFERENCES etl_runs(etl_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE offense_types (
    offense_code VARCHAR(10) PRIMARY KEY,
    offense_name VARCHAR(100) NOT NULL,
    offense_parent_group VARCHAR(100),
    group_a_b CHAR(1),
    crime_against_category VARCHAR(50)
);

CREATE TABLE report_offenses (
    offense_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_number VARCHAR(50) NOT NULL,
    offense_code VARCHAR(10) NOT NULL,
    offense_date DATETIME NOT NULL COMMENT 'Primary offense date from SPD data',
    offense_start_time DATETIME NULL,
    offense_end_time DATETIME NULL,
    etl_id INT,
    FOREIGN KEY (report_number) REFERENCES crime_reports(report_number)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (offense_code) REFERENCES offense_types(offense_code)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (etl_id) REFERENCES etl_runs(etl_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX idx_report_number (report_number),
    INDEX idx_offense_code (offense_code)
);

-- //////////////////////////////////////////////////////
-- Real-Time Data
-- //////////////////////////////////////////////////////
CREATE TABLE realtime_incidents (
    incident_id VARCHAR(50) PRIMARY KEY,
    incident_type VARCHAR(100),
    event_datetime DATETIME,
    address VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,
    report_location VARCHAR(255),
    source_id INT,
    FOREIGN KEY (source_id) REFERENCES sources(source_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

-- //////////////////////////////////////////////////////
-- GIS Streets Data
-- //////////////////////////////////////////////////////
CREATE TABLE intersections (
    intkey VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    direction VARCHAR(10),
    gis_x DOUBLE,
    gis_y DOUBLE
);

CREATE TABLE street_segments (
    unitid VARCHAR(50) PRIMARY KEY,
    onstreet VARCHAR(255),
    speedlimit INT,
    artclass INT,
    status VARCHAR(50),
    seglength INT,
    surfacewidth INT,
    slope_pct DOUBLE,
    owner VARCHAR(100),
    oneway CHAR(1),
    flow VARCHAR(50),
    gis_mid_x DOUBLE,
    gis_mid_y DOUBLE,
    start_intkey VARCHAR(50),
    end_intkey VARCHAR(50),
    FOREIGN KEY (start_intkey) REFERENCES intersections(intkey)
        ON UPDATE CASCADE ON DELETE SET NULL,
    FOREIGN KEY (end_intkey) REFERENCES intersections(intkey)
        ON UPDATE CASCADE ON DELETE SET NULL
);

-- //////////////////////////////////////////////////////
-- App Users & Alerts
-- //////////////////////////////////////////////////////
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    radius_m INT,
    center_lat DOUBLE,
    center_lon DOUBLE,
    crime_type_filter VARCHAR(100),
    active_flag BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);