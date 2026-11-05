package com.affiliatelink.offre.dto;

import com.affiliatelink.offre.entity.enums.CampagneStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCampagneStatusRequest(
        @NotNull CampagneStatus status
) {
}