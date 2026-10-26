-- Роли
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE -- 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_DOCTOR'
);

-- Пользователи
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE, -- email или логин
    password VARCHAR(255) NOT NULL,        -- BCrypt хеш
    full_name VARCHAR(200),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Связь пользователей и ролей
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Начальные данные
INSERT INTO roles (name) VALUES
    ('ROLE_ADMIN'), ('ROLE_MANAGER'), ('ROLE_DOCTOR');