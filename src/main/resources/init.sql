-- Initialize database schema and seed data
-- Seed default users if not exists
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (username, email, password, role, company_name, enabled)
SELECT * FROM (SELECT
    'admin' AS username,
    'admin@greenchains.com' AS email,
    '$2a$10$E3QnX2NfG9a8c4vB5d6eFgHhIjKlMnOpQrStUvWxYzAbCdEfGhIiJ' AS password,
    'ADMIN' AS role,
    'GreenChain Admin' AS company_name,
    true AS enabled
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, email, password, role, company_name, enabled)
SELECT * FROM (SELECT
    'manager' AS username,
    'manager@greenchains.com' AS email,
    '$2a$10$E3QnX2NfG9a8c4vB5d6eFgHhIjKlMnOpQrStUvWxYzAbCdEfGhIiJ' AS password,
    'SUSTAINABILITY_MANAGER' AS role,
    'Sustainability Team' AS company_name,
    true AS enabled
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'manager');

-- VIEWER user for testing
-- Password: viewer123 (BCrypt encoded)
INSERT INTO users (username, email, password, role, company_name, enabled)
SELECT * FROM (SELECT
    'viewer' AS username,
    'viewer@greenchains.com' AS email,
    '$2a$10$E3QnX2NfG9a8c4vB5d6eFgHhIjKlMnOpQrStUvWxYzAbCdEfGhIiJ' AS password,
    'VIEWER' AS role,
    'Viewer Department' AS company_name,
    true AS enabled
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'viewer');

-- Another VIEWER user for additional testing
INSERT INTO users (username, email, password, role, company_name, enabled)
SELECT * FROM (SELECT
    'analyst' AS username,
    'analyst@greenchains.com' AS email,
    '$2a$10$E3QnX2NfG9a8c4vB5d6eFgHhIjKlMnOpQrStUvWxYzAbCdEfGhIiJ' AS password,
    'VIEWER' AS role,
    'Analysis Team' AS company_name,
    true AS enabled
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'analyst');
