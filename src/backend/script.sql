-- ============================================================
--  HANGER — Schema do Banco de Dados
--  PostgreSQL / Supabase
-- ============================================================

-- Habilita extensão para UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
--  ENUM TYPES
-- ============================================================

CREATE TYPE notification_type AS ENUM ('like', 'follow', 'comment', 'share');
CREATE TYPE platform_type     AS ENUM ('android', 'ios');

-- ============================================================
--  USERS
-- ============================================================

CREATE TABLE users (
    id            UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    username      VARCHAR(50) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    bio           TEXT,
    avatar_url    VARCHAR(500),
    location_city VARCHAR(100),
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ============================================================
--  CATEGORIES  (ex: Casual, Formal, Esportivo)
-- ============================================================

CREATE TABLE categories (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================================
--  TYPES  (ex: Casual → Street, Básico; Formal → Executivo)
-- ============================================================

CREATE TABLE types (
    id          SERIAL      PRIMARY KEY,
    category_id INT         NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name        VARCHAR(50) NOT NULL,
    UNIQUE (category_id, name)
);

-- ============================================================
--  POSTS
-- ============================================================

CREATE TABLE posts (
    id                UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id           UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    image_url         VARCHAR(500) NOT NULL,
    title             VARCHAR(150) NOT NULL,
    caption           TEXT,
    -- Dados vindos da API de clima no momento da publicação
    weather_condition VARCHAR(50),   -- ex: "sunny", "rainy", "cloudy"
    temperature       FLOAT,         -- em °C
    city              VARCHAR(100),  -- cidade no momento do post
    share_count       INT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
--  POST_TAGS  (N:N entre posts, categories e types)
-- ============================================================

CREATE TABLE post_tags (
    post_id     UUID NOT NULL REFERENCES posts(id)      ON DELETE CASCADE,
    category_id INT  NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    type_id     INT           REFERENCES types(id)      ON DELETE SET NULL,
    PRIMARY KEY (post_id, category_id)
);

-- ============================================================
--  FOLLOWS
-- ============================================================

CREATE TABLE follows (
    follower_id  UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id),
    -- Usuário não pode seguir a si mesmo
    CONSTRAINT no_self_follow CHECK (follower_id <> following_id)
);

-- ============================================================
--  LIKES
-- ============================================================

CREATE TABLE likes (
    user_id    UUID      NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    post_id    UUID      NOT NULL REFERENCES posts(id)  ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, post_id)
);

-- ============================================================
--  COMMENTS
-- ============================================================

CREATE TABLE comments (
    id         UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id    UUID      NOT NULL REFERENCES posts(id)  ON DELETE CASCADE,
    user_id    UUID      NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    content    TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
--  NOTIFICATIONS
-- ============================================================

CREATE TABLE notifications (
    id           UUID              PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipient_id UUID              NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sender_id    UUID              NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         notification_type NOT NULL,
    post_id      UUID              REFERENCES posts(id) ON DELETE CASCADE,
    read         BOOLEAN           NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP         NOT NULL DEFAULT NOW()
);

-- ============================================================
--  DEVICE_TOKENS  (push notifications — FCM / APNs)
-- ============================================================

CREATE TABLE device_tokens (
    id         UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(500)  NOT NULL UNIQUE,
    platform   platform_type NOT NULL,
    updated_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ============================================================
--  ÍNDICES  (performance nas queries mais comuns)
-- ============================================================

-- Feed: posts ordenados por data
CREATE INDEX idx_posts_created_at     ON posts(created_at DESC);
-- Posts de um usuário específico (tela de perfil)
CREATE INDEX idx_posts_user_id        ON posts(user_id);
-- Busca por cidade/clima
CREATE INDEX idx_posts_city           ON posts(city);
-- Notificações não lidas de um usuário
CREATE INDEX idx_notif_recipient_read ON notifications(recipient_id, read);
-- Busca por tags
CREATE INDEX idx_post_tags_category   ON post_tags(category_id);
CREATE INDEX idx_post_tags_type       ON post_tags(type_id);
-- Seguidores
CREATE INDEX idx_follows_following    ON follows(following_id);

-- ============================================================
--  SEED  — Dados iniciais de Categories e Types
-- ============================================================

INSERT INTO categories (name) VALUES
    ('Casual'),
    ('Formal'),
    ('Esportivo'),
    ('Festa'),
    ('Praia'),
    ('Inverno'),
    ('Streetwear');

INSERT INTO types (category_id, name) VALUES
    -- Casual (id=1)
    (1, 'Básico'),
    (1, 'Minimalista'),
    (1, 'Colorido'),
    -- Formal (id=2)
    (2, 'Executivo'),
    (2, 'Social'),
    (2, 'Business Casual'),
    -- Esportivo (id=3)
    (3, 'Academia'),
    (3, 'Running'),
    (3, 'Outdoor'),
    -- Festa (id=4)
    (4, 'Balada'),
    (4, 'Casamento'),
    (4, 'Casual Chic'),
    -- Praia (id=5)
    (5, 'Biquíni / Sunga'),
    (5, 'Saída de praia'),
    -- Inverno (id=6)
    (6, 'Layering'),
    (6, 'Aconchegante'),
    -- Streetwear (id=7)
    (7, 'Hype'),
    (7, 'Skate'),
    (7, 'Grunge');