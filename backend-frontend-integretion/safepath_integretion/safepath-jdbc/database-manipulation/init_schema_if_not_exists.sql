-- Initialize schema only if tables don't exist
-- This script should only create tables if they don't already exist

-- Create sources table if not exists
CREATE TABLE IF NOT EXISTS sources (
    source_id INT AUTO_INCREMENT PRIMARY KEY,
    source_name VARCHAR(255) NOT NULL,
    url VARCHAR(255),
    refresh_interval INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create etl_runs table if not exists
CREATE TABLE IF NOT EXISTS etl_runs (
    etl_id INT AUTO_INCREMENT PRIMARY KEY,
    source_id INT NOT NULL,
    run_time DATETIME NOT NULL,
    record_count INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id) REFERENCES sources(source_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Create crime_reports table if not exists
CREATE TABLE IF NOT EXISTS crime_reports (
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

-- Create offense_types table if not exists
CREATE TABLE IF NOT EXISTS offense_types (
    offense_code VARCHAR(10) PRIMARY KEY,
    offense_name VARCHAR(100) NOT NULL,
    offense_parent_group VARCHAR(100),
    group_a_b CHAR(1),
    crime_against_category VARCHAR(50)
);

-- Create report_offenses table if not exists
CREATE TABLE IF NOT EXISTS report_offenses (
    offense_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_number VARCHAR(50) NOT NULL,
    offense_code VARCHAR(10) NOT NULL,
    offense_date DATETIME NOT NULL,
    offense_start_time DATETIME,
    offense_end_time DATETIME,
    etl_id INT,
    FOREIGN KEY (report_number) REFERENCES crime_reports(report_number)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (offense_code) REFERENCES offense_types(offense_code)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (etl_id) REFERENCES etl_runs(etl_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

-- Create realtime_incidents table if not exists
CREATE TABLE IF NOT EXISTS realtime_incidents (
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

-- Create intersections table if not exists
CREATE TABLE IF NOT EXISTS intersections (
    intkey VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    direction VARCHAR(10),
    gis_x DOUBLE,
    gis_y DOUBLE
);

-- Create street_segments table if not exists
CREATE TABLE IF NOT EXISTS street_segments (
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

-- Create users table if not exists
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create user_alerts table if not exists
CREATE TABLE IF NOT EXISTS user_alerts (
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

