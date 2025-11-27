-- =============================================================================
-- Datos de prueba para Spring Security (PostgreSQL)
-- Password por defecto: "password" con BCrypt
-- =============================================================================

-- Usuario administrador
INSERT INTO users (username, password, enabled)
VALUES ('admin', '{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW',
        TRUE) ON CONFLICT (username) DO NOTHING;

-- Usuario normal
INSERT INTO users (username, password, enabled)
VALUES ('user', '{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW',
        TRUE) ON CONFLICT (username) DO NOTHING;

-- ===========================
-- OPCIÓN 1: Authorities Directas
-- ===========================
-- INSERT INTO authorities (username, authority)
-- VALUES ('admin', 'ROLE_ADMIN')
--     ON CONFLICT (username, authority) DO NOTHING;
--
-- INSERT INTO authorities (username, authority)
-- VALUES ('admin', 'ROLE_USER')
--     ON CONFLICT (username, authority) DO NOTHING;
--
-- INSERT INTO authorities (username, authority)
-- VALUES ('user', 'ROLE_USER')
--     ON CONFLICT (username, authority) DO NOTHING;

-- ===========================
-- OPCIÓN 2: Groups (comentar si usas authorities directas)
-- ===========================
INSERT INTO groups (group_name)
VALUES ('ADMINISTRATORS') ON CONFLICT (group_name) DO NOTHING;

INSERT INTO groups (group_name)
VALUES ('USERS') ON CONFLICT (group_name) DO NOTHING;

INSERT INTO group_authorities (group_id, authority)
SELECT id, 'ROLE_ADMIN'
FROM groups
WHERE group_name = 'ADMINISTRATORS' ON CONFLICT (group_id, authority) DO NOTHING;

INSERT INTO group_authorities (group_id, authority)
SELECT id, 'ROLE_PRUEBA'
FROM groups
WHERE group_name = 'ADMINISTRATORS' ON CONFLICT (group_id, authority) DO NOTHING;

INSERT INTO group_authorities (group_id, authority)
SELECT id, 'ROLE_USER'
FROM groups
WHERE group_name = 'ADMINISTRATORS' ON CONFLICT (group_id, authority) DO NOTHING;

INSERT INTO group_authorities (group_id, authority)
SELECT id, 'ROLE_USER'
FROM groups
WHERE group_name = 'USERS' ON CONFLICT (group_id, authority) DO NOTHING;

INSERT INTO group_members (username, group_id)
SELECT 'admin', id
FROM groups
WHERE group_name = 'ADMINISTRATORS' ON CONFLICT (username, group_id) DO NOTHING;

INSERT INTO group_members (username, group_id)
SELECT 'user', id
FROM groups
WHERE group_name = 'USERS' ON CONFLICT (username, group_id) DO NOTHING;