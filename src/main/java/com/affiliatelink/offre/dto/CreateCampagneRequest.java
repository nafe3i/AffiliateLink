package com.affiliatelink.offre.dto;

import com.affiliatelink.offre.entity.enums.PayoutModel;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCampagneRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotNull
        @Positive
        @Digits(integer = 12, fraction = 2)
        BigDecimal budget,

        @NotNull
        PayoutModel payoutModel,

        @NotNull
        @Positive
        @Digits(integer = 12, fraction = 2)
        BigDecimal payoutValue

) {
}