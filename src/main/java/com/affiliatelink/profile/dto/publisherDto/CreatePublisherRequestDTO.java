package com.affiliatelink.profile.dto.publisherDto;

import com.affiliatelink.profile.entity.enums.Niche;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;


public record CreatePublisherRequestDTO(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotNull(message = "Niche is required")
        Niche niche,

        @NotBlank(message = "Bio is required")
        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country

) {
}