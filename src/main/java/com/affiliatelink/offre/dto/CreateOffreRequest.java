package com.affiliatelink.offre.dto;

import com.affiliatelink.common.domain.Niche;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateOffreRequest(

        @NotNull
        @Positive
        Long advertiserId,

        @NotBlank
        @Size(max = 255)
        String title,

        String description,

        @NotBlank
        @Size(max = 255)
        String landingUrl,

        @NotNull
        Niche niche,

        @NotNull
        @Size(max = 5)
        List<@NotBlank String> tags

) {
}