-- Phase 1 has no real auth or accounts yet (see backend-api-spec.md, "Phase 1 usage" / Auth section).
-- Every request resolves to this one fixed, well-known user id. email/password_hash stay NULL -
-- they're unused until Phase 2 real accounts exist.
INSERT INTO users (id, email, password_hash)
VALUES ('00000000-0000-0000-0000-000000000001', NULL, NULL);
