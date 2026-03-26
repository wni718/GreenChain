-- Initialize database schema and seed data
-- Seed default users if not exists
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (username, email, password, role, company_name, enabled)
SELECT * FROM (SELECT
    'admin' AS username,
    'admin@greenchains.com' AS email,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.a.t4w2W3Q0p1J7vCqO' AS password,
    'ADMIN' AS role,
    'GreenChain Admin' AS company_name,
    true AS enabled
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, email, password, role, company_name, enabled)
SELECT * FROM (SELECT
    'manager' AS username,
    'manager@greenchains.com' AS email,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.a.t4w2W3Q0p1J7vCqO' AS password,
    'SUSTAINABILITY_MANAGER' AS role,
    'Sustainability Team' AS company_name,
    true AS enabled
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'manager');
