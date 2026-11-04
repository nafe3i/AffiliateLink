package com.affiliatelink.offre.dto;

import com.affiliatelink.common.domain.Niche;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateOffreRequest(

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
        List<@NotBlank @Size(max = 255) String> tags

) {
}