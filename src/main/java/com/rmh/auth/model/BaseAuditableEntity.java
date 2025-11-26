package com.rmh.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;

/**
 * Provides audit timestamps and soft-delete behavior for entities.
 */
@MappedSuperclass
public abstract class BaseAuditableEntity {

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "timestamp with time zone default CURRENT_TIMESTAMP"
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            columnDefinition = "timestamp with time zone default CURRENT_TIMESTAMP"
    )
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    protected void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
        // Always update deletedAt for audit purposes, even if already inactive
        // This ensures we track when deactivation was attempted/confirmed
        this.deletedAt = Instant.now();
    }

    public void reactivate() {
        this.active = true;
        this.deletedAt = null;
    }

    protected void setActive(boolean active) {
        this.active = active;
    }
}

