-- Initialize database schema and seed data

-- Create transport modes if not exists
INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'AIR', 'Air Freight', 1.2) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'AIR');

INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'SEA', 'Sea Freight', 0.05) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'SEA');

INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'TRUCK', 'Truck Transport', 0.2) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'TRUCK');

INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'RAIL', 'Rail Transport', 0.1) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'RAIL');

-- Create default users if not exists
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (username, email, password, role, company_name, enabled)
SELECT * FROM (SELECT 'admin', 'admin@greenchains.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.a.t4w2W3Q0p1J7vCqO', 'ADMIN', 'GreenChain Admin', true) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, email, password, role, company_name, enabled)
SELECT * FROM (SELECT 'manager', 'manager@greenchains.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.a.t4w2W3Q0p1J7vCqO', 'SUSTAINABILITY_MANAGER', 'Sustainability Team', true) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'manager');
