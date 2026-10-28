package com.affiliatelink.offre.entity;

import com.affiliatelink.offre.entity.enums.CampagneStatus;
import com.affiliatelink.offre.entity.enums.PayoutModel;
import jakarta.persistence.*;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "offre_offres_compagne")
public class Campagne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_model", nullable = false)
    private PayoutModel payoutModel;

    @Column(name = "payout_value", nullable = false,
            precision = 14,
            scale = 2)
    private BigDecimal payoutValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampagneStatus status = CampagneStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offre_id", nullable = false)
    private Offre offre;

}
