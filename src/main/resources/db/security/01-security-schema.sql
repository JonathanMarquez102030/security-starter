-- =============================================================================
-- Spring Security Standard Tables Schema
-- Compatible con PostgreSQL y JDBCUserDetailsManager
-- =============================================================================

-- Tabla principal de usuarios
CREATE TABLE IF NOT EXISTS users
(
    username
    VARCHAR
(
    50
) NOT NULL PRIMARY KEY,
    password VARCHAR
(
    500
) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Tabla de autoridades directas por usuario
CREATE TABLE IF NOT EXISTS authorities
(
    username
    VARCHAR
(
    50
) NOT NULL,
    authority VARCHAR
(
    50
) NOT NULL,
    CONSTRAINT fk_authorities_users
    FOREIGN KEY
(
    username
) REFERENCES users
(
    username
) ON DELETE CASCADE
    );

CREATE UNIQUE INDEX IF NOT EXISTS ix_auth_username
    ON authorities (username, authority);

-- Tabla de grupos
CREATE TABLE IF NOT EXISTS groups
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    group_name
    VARCHAR
(
    50
) NOT NULL UNIQUE
    );

-- Tabla de autoridades por grupo
CREATE TABLE IF NOT EXISTS group_authorities
(
    group_id
    BIGINT
    NOT
    NULL,
    authority
    VARCHAR
(
    50
) NOT NULL,
    CONSTRAINT fk_group_authorities_group
    FOREIGN KEY
(
    group_id
) REFERENCES groups
(
    id
) ON DELETE CASCADE,
    CONSTRAINT uk_group_authorities UNIQUE
(
    group_id,
    authority
)
    );

-- Tabla de miembros de grupos
CREATE TABLE IF NOT EXISTS group_members
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    username
    VARCHAR
(
    50
) NOT NULL,
    group_id BIGINT NOT NULL,
    CONSTRAINT fk_group_members_user
    FOREIGN KEY
(
    username
) REFERENCES users
(
    username
) ON DELETE CASCADE,
    CONSTRAINT fk_group_members_group
    FOREIGN KEY
(
    group_id
) REFERENCES groups
(
    id
)
  ON DELETE CASCADE,
    CONSTRAINT uk_group_members UNIQUE
(
    username,
    group_id
)
    );