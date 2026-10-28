package com.affiliatelink.profile.dto.publisherDto;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.profile.entity.enums.ProfileStatus;

import java.time.Instant;

public record PublisherResponseDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        Niche niche,
        String bio,
        String country,
        ProfileStatus status,
        Instant createdAt
) {
}