package com.linguanest.backend.user;

import java.util.UUID;

/**
 * Phase 1 has no real auth or per-person accounts (see backend-api-spec.md, Auth section). Every
 * request - regardless of who is actually calling - resolves to this single fixed user id, which
 * is seeded into the {@code users} table by {@code V2__seed_presumed_user.sql}.
 *
 * <p>Centralized here so the eventual Phase 2 auth filter has exactly one place to replace with a
 * real per-request resolved user id.
 */
public final class PresumedUser {

    public static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private PresumedUser() {
    }
}
