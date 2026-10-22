package com.affiliatelink.profile.dto.advertiserDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAdvertiserRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "companyName is required")
        @Size(max = 255, message = "compnyName must not exceed 255 characters")
        String companyName,

        @NotBlank(message = "secotr is required")
        @Size(max = 255, message = "sector must not exceed 255 characters")
        String sector,
        @NotBlank(message = "website is required")
        @Size(max = 255, message = "website must not exceed 255 characters")
        String website
) {


}
