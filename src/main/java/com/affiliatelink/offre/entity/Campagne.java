package com.affiliatelink.offre.entity;

import com.affiliatelink.common.exception.CampagneNotValidateException;
import com.affiliatelink.offre.entity.enums.CampagneStatus;
import com.affiliatelink.offre.entity.enums.PayoutModel;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "offre_campagnes")
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

    @Column(
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal budget;

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

    protected Campagne() {
    }


    public Campagne(
            Offre offre,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal budget,
            PayoutModel payoutModel,
            BigDecimal payoutValue
    ) {

        // 1. L'offre est obligatoire
        if (offre == null) {
            throw new IllegalArgumentException(
                    "Une campagne doit appartenir à une offre"
            );
        }

        // 2. Le nom est obligatoire
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom de la campagne est obligatoire"
            );
        }

        name = name.strip();

        if (name.length() > 255) {
            throw new IllegalArgumentException(
                    "Le nom ne doit pas dépasser 255 caractères"
            );
        }

        // 3. Vérification des dates
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Les dates sont obligatoires"
            );
        }

        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException(
                    "La date de fin doit être après la date de début"
            );
        }

        // 4. Vérification du budget
        if (budget == null ||
                budget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Le budget doit être strictement positif"
            );
        }

        // 5. Le modèle de rémunération est obligatoire
        if (payoutModel == null) {
            throw new IllegalArgumentException(
                    "Le modèle de rémunération est obligatoire"
            );
        }

        // 6. Vérification de la rémunération
        if (payoutValue == null ||
                payoutValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CampagneNotValidateException(
                    "La rémunération doit être strictement positive"
            );
        }

        // 7. Règle particulière au CPA_PERCENT
        if (payoutModel == PayoutModel.CPA_PERCENT &&
                payoutValue.compareTo(BigDecimal.valueOf(100)) >= 0) {
            throw new CampagneNotValidateException(
                    "Le pourcentage doit être inférieur à 100"
            );
        }

        // 8. Respect de NUMERIC(14,2) dans PostgreSQL
        BigDecimal max = new BigDecimal("999999999999.99");

        if (budget.compareTo(max) > 0 ||
                payoutValue.compareTo(max) > 0) {
            throw new CampagneNotValidateException(
                    "La valeur dépasse la précision autorisée"
            );
        }

        if (budget.stripTrailingZeros().scale() > 2 ||
                payoutValue.stripTrailingZeros().scale() > 2) {
            throw new CampagneNotValidateException(
                    "Deux chiffres décimaux maximum sont autorisés"
            );
        }

        // 9. Initialisation de l'entité
        this.offre = offre;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.payoutModel = payoutModel;
        this.payoutValue = payoutValue;

        // Toute nouvelle campagne commence en DRAFT
        this.status = CampagneStatus.DRAFT;
    }


}
