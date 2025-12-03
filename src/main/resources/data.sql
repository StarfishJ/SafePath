-- Sample intersections
INSERT IGNORE INTO intersections (intkey, name, direction, gis_x, gis_y)
VALUES
    ('INT001', '1ST AVE & PINE', 'N', 47.6097, -122.3415),
    ('INT002', '1ST AVE & UNION', 'N', 47.6084, -122.3397),
    ('INT003', '3RD AVE & PIKE', 'N', 47.6102, -122.3385);

-- Sample street segments
INSERT IGNORE INTO street_segments (
    unitid,
    onstreet,
    speedlimit,
    artclass,
    status,
    seglength,
    surfacewidth,
    slope_pct,
    owner,
    oneway,
    flow,
    gis_mid_x,
    gis_mid_y,
    start_intkey,
    end_intkey
)
VALUES
    ('SEG001', '1ST AVE', 25, 1, 'OPEN', 120, 12, 2.5, 'CITY', 'N', 'TWO WAY', -122.3406, 47.6090, 'INT001', 'INT002'),
    ('SEG002', '3RD AVE', 25, 1, 'OPEN', 150, 14, 3.1, 'CITY', 'N', 'TWO WAY', -122.3391, 47.6100, 'INT002', 'INT003'),
    ('SEG003', 'UNION ST', 20, 2, 'OPEN', 110, 11, 1.2, 'CITY', 'N', 'TWO WAY', -122.3380, 47.6087, 'INT002', 'INT003');

-- Sample risk scores (seeded for H2 demos)
INSERT IGNORE INTO street_segment_risk (
    unitid,
    cluster_id,
    risk_label,
    risk_score,
    incident_density,
    night_fraction,
    last_90d_incidents,
    model_version,
    override_reason,
    updated_at
)
VALUES
    ('SEG001', 2, 'HIGH', 0.85, 0.18, 0.52, 18, 'kmeans_c1_v1', '18 incidents in 90d, night 52%, trend x1.40', CURRENT_TIMESTAMP()),
    ('SEG002', 1, 'MEDIUM', 0.45, 0.09, 0.33, 9, 'kmeans_c1_v1', '9 incidents in 90d, night 33%, trend x1.05', CURRENT_TIMESTAMP()),
    ('SEG003', 0, 'LOW', 0.10, 0.02, 0.10, 2, 'kmeans_c1_v1', '2 incidents in 90d, night 10%, trend x0.75', CURRENT_TIMESTAMP());

-- Insert three data into users
INSERT IGNORE INTO users (email, password_hash)
VALUES
    ('admin@safepath.com', 'hash_admin123'),
    ('alice@gmail.com', 'hash_alice123'),
    ('bob@gmail.com', 'hash_bob123');

-- Insert three data into user_alerts
INSERT IGNORE INTO user_alerts (user_id, radius_m, center_lat, center_lon, crime_type_filter)
VALUES
    (1, 1000, 47.6062, -122.3321, 'ASSAULT'),
    (2, 500, 47.6205, -122.3493, 'BURGLARY'),
    (3, 1500, 47.6097, -122.3331, 'ROBBERY');