-- Insert three data into users (only if they don't exist to avoid duplicates)
INSERT INTO users (email, password_hash)
SELECT 'admin@safepath.com', 'hash_admin123'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@safepath.com');

INSERT INTO users (email, password_hash)
SELECT 'alice@gmail.com', 'hash_alice123'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'alice@gmail.com');

INSERT INTO users (email, password_hash)
SELECT 'bob@gmail.com', 'hash_bob123'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'bob@gmail.com');

-- Verify users were inserted and show their user_id
SELECT user_id, email FROM users WHERE email IN ('admin@safepath.com', 'alice@gmail.com', 'bob@gmail.com') ORDER BY user_id;

-- Insert three data into user_alerts
-- IMPORTANT: Uses actual user_id from users table (works with any user_id values: 1,2,3 or 5,6,7, etc.)
INSERT INTO user_alerts (user_id, radius_m, center_lat, center_lon, crime_type_filter)
SELECT u.user_id, 1000, 47.6062, -122.3321, 'ASSAULT'
FROM users u
WHERE u.email = 'admin@safepath.com'
  AND NOT EXISTS (SELECT 1 FROM user_alerts ua WHERE ua.user_id = u.user_id);

INSERT INTO user_alerts (user_id, radius_m, center_lat, center_lon, crime_type_filter)
SELECT u.user_id, 500, 47.6205, -122.3493, 'BURGLARY'
FROM users u
WHERE u.email = 'alice@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM user_alerts ua WHERE ua.user_id = u.user_id);

INSERT INTO user_alerts (user_id, radius_m, center_lat, center_lon, crime_type_filter)
SELECT u.user_id, 1500, 47.6097, -122.3331, 'ROBBERY'
FROM users u
WHERE u.email = 'bob@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM user_alerts ua WHERE ua.user_id = u.user_id);

-- Verify the inserts
SELECT 'User alerts inserted:' as status, ua.alert_id, ua.user_id, u.email, ua.crime_type_filter
FROM user_alerts ua
JOIN users u ON ua.user_id = u.user_id
WHERE u.email IN ('admin@safepath.com', 'alice@gmail.com', 'bob@gmail.com')
ORDER BY ua.alert_id;