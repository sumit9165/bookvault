-- ═══════════════════════════════════════════════════════════════
-- BookVault - V3 Performance Indexes
-- Compatible with: MySQL 8.x and MariaDB 10.x / 11.x / 12.x
-- ═══════════════════════════════════════════════════════════════

-- Composite index for common admin query: active + created_at sort
ALTER TABLE books
  ADD INDEX idx_books_active_created (deleted_at, created_at);

-- Composite index for public listing: public + not-deleted + created sort
ALTER TABLE books
  ADD INDEX idx_books_public_active (is_public, deleted_at, created_at);

-- Covering index for the public listing API (title, author, version only)
-- Avoids table scan when fetching only public columns
ALTER TABLE books
  ADD INDEX idx_books_public_cover (is_public, deleted_at, title, author, version_release);

-- Audit log — faster lookups by user + action
ALTER TABLE audit_logs
  ADD INDEX idx_audit_user_action (user_id, action, created_at);

-- Refresh tokens — composite for fast active-token count per user
ALTER TABLE refresh_tokens
  ADD INDEX idx_rt_user_revoked_expires (user_id, revoked, expires_at);
