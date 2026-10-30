package com.affiliatelink.offre.dto;

import com.affiliatelink.common.domain.Niche;

import java.time.Instant;
import java.util.List;

public record OffreResponse(

        Long id,
        Long advertiserId,
        String title,
        String description,
        String landingUrl,
        Niche niche,
        Instant createdAt,
        List<String> tags

) {
}