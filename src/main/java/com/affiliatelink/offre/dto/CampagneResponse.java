package com.affiliatelink.offre.dto;

import com.affiliatelink.offre.entity.enums.CampagneStatus;
import com.affiliatelink.offre.entity.enums.PayoutModel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CampagneResponse(

        Long id,

        Long offreId,

        String name,

        LocalDate startDate,

        LocalDate endDate,

        BigDecimal budget,

        PayoutModel payoutModel,

        BigDecimal payoutValue,

        CampagneStatus status

) {
}
