-- ═══════════════════════════════════════════════════════════════
-- BookVault - V1 Initial Schema
-- Compatible with: MySQL 8.x and MariaDB 10.x / 11.x / 12.x
-- Notes:
--   • ENUM, BOOLEAN, FULLTEXT, InnoDB, utf8mb4 all supported on both
--   • DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP works on both
--   • AUTO_INCREMENT works on both
-- ═══════════════════════════════════════════════════════════════

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)     NOT NULL,
    email       VARCHAR(255)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    role        ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login  DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Books Table
CREATE TABLE IF NOT EXISTS books (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    title           VARCHAR(500)    NOT NULL,
    author          VARCHAR(300)    NOT NULL,
    isbn            VARCHAR(20),
    version_release VARCHAR(50)     NOT NULL,
    description     TEXT,
    genre           VARCHAR(100),
    publisher       VARCHAR(300),
    language        VARCHAR(50)     DEFAULT 'English',
    page_count      INT,
    cover_image_url VARCHAR(1000),
    pdf_file_path   VARCHAR(1000),
    pdf_file_name   VARCHAR(500),
    pdf_file_size   BIGINT,
    is_public       BOOLEAN         NOT NULL DEFAULT TRUE,
    view_count      BIGINT          NOT NULL DEFAULT 0,
    created_by      BIGINT,
    updated_by      BIGINT,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_books_isbn (isbn),
    INDEX idx_books_author (author),
    INDEX idx_books_genre (genre),
    INDEX idx_books_is_public (is_public),
    INDEX idx_books_deleted_at (deleted_at),
    INDEX idx_books_created_at (created_at),
    FULLTEXT INDEX ft_books_search (title, author, description),
    CONSTRAINT fk_books_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_books_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Refresh Tokens Table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    token       VARCHAR(512)    NOT NULL,
    expires_at  DATETIME        NOT NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked     BOOLEAN         NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_user_id (user_id),
    INDEX idx_refresh_tokens_expires_at (expires_at),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit Log Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT,
    action      VARCHAR(100)    NOT NULL,
    entity_type VARCHAR(100),
    entity_id   BIGINT,
    details     TEXT,
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(500),
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_audit_user_id (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Rate Limit Tracking (for DDoS protection backup)
CREATE TABLE IF NOT EXISTS rate_limit_violations (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    ip_address  VARCHAR(45)     NOT NULL,
    endpoint    VARCHAR(200),
    count       INT             NOT NULL DEFAULT 1,
    blocked_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  DATETIME        NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_rate_limit_ip (ip_address),
    INDEX idx_rate_limit_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
