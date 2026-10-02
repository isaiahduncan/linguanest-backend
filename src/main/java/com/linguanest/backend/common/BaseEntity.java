package com.linguanest.backend.common;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Shared {@code id}/{@code created_at} mapping - every table in the Phase 1 schema uses the same
 * UUID id generation and creation-timestamp convention (see backend-api-spec.md, Database
 * Schema), so it's extracted here once rather than repeated verbatim across every entity.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
