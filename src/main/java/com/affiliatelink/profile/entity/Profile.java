package com.affiliatelink.profile.entity;

import com.affiliatelink.profile.entity.enums.ProfileStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "profile_profiles")
public abstract class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus status = ProfileStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Profile() {
    }


    protected Profile(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public ProfileStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void activate() {
        if (status != ProfileStatus.PENDING
                && status != ProfileStatus.SUSPENDED) {
            throw new IllegalStateException(
                    "Activation impossible depuis " + status
            );
        }

        status = ProfileStatus.ACTIVE;
    }

    public void suspend() {
        if (status != ProfileStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Suspension impossible depuis " + status
            );
        }

        status = ProfileStatus.SUSPENDED;
    }


}
