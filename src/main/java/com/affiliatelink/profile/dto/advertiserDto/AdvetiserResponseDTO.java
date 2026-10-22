package com.affiliatelink.profile.dto.advertiserDto;

public record AdvetiserResponseDTO(
        Long id,
        String email,
        String companyName,
        String sector,
        String website) {
}
