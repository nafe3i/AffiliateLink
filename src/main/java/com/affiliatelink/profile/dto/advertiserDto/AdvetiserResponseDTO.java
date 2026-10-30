package com.affiliatelink.profile.dto.advertiserDto;

import com.affiliatelink.profile.entity.enums.ProfileStatus;

public record AdvetiserResponseDTO(
        Long id,
        String email,
        String companyName,
        String sector,
        String website,
        ProfileStatus profileStatus) {
}
