package com.linguanest.backend.user;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A person using the app. In Phase 1 there is no real auth or account creation yet - every
 * request resolves to a single hardcoded/presumed row (see {@link PresumedUser}), so
 * {@code email}/{@code passwordHash} stay unused and null until Phase 2 real accounts exist.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "email", length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
