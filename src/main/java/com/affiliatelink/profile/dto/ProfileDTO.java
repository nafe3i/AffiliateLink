package com.affiliatelink.profile.dto;

import com.affiliatelink.profile.entity.enums.ProfileStatus;

public record ProfileDTO(Long id, String email, ProfileStatus status) {
}
