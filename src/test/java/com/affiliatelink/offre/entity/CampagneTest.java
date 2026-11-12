package com.affiliatelink.offre.entity;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.offre.CampagneNotValidateException;
import com.affiliatelink.common.exception.offre.CampagneStatusConflictException;
import com.affiliatelink.offre.entity.enums.CampagneStatus;
import com.affiliatelink.offre.entity.enums.PayoutModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CampagneTest {
    private static final LocalDate START = LocalDate.of(2030, 1, 1);
    private static final LocalDate END = LocalDate.of(2030, 2, 1);
    private static final LocalDate TEST_DAY = LocalDate.of(2030, 1, 15);

    private static Offre offre() {
        return new Offre(1L, "Offre de test", "Description", "https://example.com", Niche.TECH);
    }

    private static Campagne campagne(PayoutModel model, String value,
                                     LocalDate start, LocalDate end) {
        return new Campagne(offre(), "Campagne", start, end,
                new BigDecimal("100.00"), model, new BigDecimal(value));
    }

    private static Campagne campagne() {
        return campagne(PayoutModel.CPC, "2.00", START, END);
    }

    // Les 5 transitions légales : DRAFT->ACTIVE, ACTIVE->PAUSED,
    // PAUSED->ACTIVE, ACTIVE->ENDED et PAUSED->ENDED.
    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("validTransitions")
    void shouldAllowValidTransitions(CampagneStatus initial, CampagneStatus target) {
        Campagne c = campagne();
        prepareStatus(c, initial);
        apply(c, target);
        assertEquals(target, c.getStatus());
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(CampagneStatus.DRAFT, CampagneStatus.ACTIVE),
                Arguments.of(CampagneStatus.ACTIVE, CampagneStatus.PAUSED),
                Arguments.of(CampagneStatus.PAUSED, CampagneStatus.ACTIVE),
                Arguments.of(CampagneStatus.ACTIVE, CampagneStatus.ENDED),
                Arguments.of(CampagneStatus.PAUSED, CampagneStatus.ENDED));
    }

    @ParameterizedTest(name = "{0} -> {1} refusée")
    @MethodSource("invalidTransitions")
    void shouldRejectForbiddenTransitions(CampagneStatus initial, CampagneStatus target) {
        Campagne c = campagne();
        prepareStatus(c, initial);
        assertThrows(CampagneStatusConflictException.class, () -> apply(c, target));
        assertEquals(initial, c.getStatus(), "Une transition refusée ne doit pas modifier le statut");
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(CampagneStatus.DRAFT, CampagneStatus.PAUSED),
                Arguments.of(CampagneStatus.DRAFT, CampagneStatus.ENDED),
                Arguments.of(CampagneStatus.ACTIVE, CampagneStatus.ACTIVE),
                Arguments.of(CampagneStatus.PAUSED, CampagneStatus.PAUSED),
                Arguments.of(CampagneStatus.ENDED, CampagneStatus.ACTIVE),
                Arguments.of(CampagneStatus.ENDED, CampagneStatus.PAUSED),
                Arguments.of(CampagneStatus.ENDED, CampagneStatus.ENDED));
    }

    private static void prepareStatus(Campagne c, CampagneStatus status) {
        switch (status) {
            case DRAFT -> { /* statut du constructeur */ }
            case ACTIVE -> c.activer(TEST_DAY);
            case PAUSED -> {
                c.activer(TEST_DAY);
                c.mettreEnPause();
            }
            case ENDED -> {
                c.activer(TEST_DAY);
                c.terminer();
            }
        }
    }

    private static void apply(Campagne c, CampagneStatus target) {
        switch (target) {
            case ACTIVE -> c.activer(TEST_DAY);
            case PAUSED -> c.mettreEnPause();
            case ENDED -> c.terminer();
            case DRAFT -> throw new IllegalArgumentException("DRAFT n'est pas une transition d'entité");
        }
    }

    @Test
    void shouldInitializeNewCampaignAsDraft() {
        assertEquals(CampagneStatus.DRAFT, campagne().getStatus());
    }

    @Test
    void shouldRejectExpiredCampaignActivation() {
        Campagne c = campagne();
        assertThrows(CampagneStatusConflictException.class,
                () -> c.activer(END.plusDays(1)));
        assertEquals(CampagneStatus.DRAFT, c.getStatus());
    }

    @Test
    void shouldAllowActivationOnEndDate() {
        Campagne c = campagne();
        c.activer(END);
        assertEquals(CampagneStatus.ACTIVE, c.getStatus());
    }

    @ParameterizedTest(name = "modèle = {0}")
    @EnumSource(value = PayoutModel.class, names = {"CPC", "CPL", "CPA_FIXED"})
    void shouldAllowPositiveFixedPayout(PayoutModel model) {
        Campagne c = campagne(model, "0.01", START, END);
        assertEquals(0, c.getPayoutValue().compareTo(new BigDecimal("0.01")));
    }

    @ParameterizedTest(name = "rémunération refusée = {1} pour {0}")
    @MethodSource("invalidPayouts")
    void shouldRejectInvalidPayout(PayoutModel model, String payout) {
        assertThrows(CampagneNotValidateException.class,
                () -> campagne(model, payout, START, END));
    }

    static Stream<Arguments> invalidPayouts() {
        return Stream.of(
                Arguments.of(PayoutModel.CPC, "0"),
                Arguments.of(PayoutModel.CPL, "-0.01"),
                Arguments.of(PayoutModel.CPA_FIXED, "0"),
                Arguments.of(PayoutModel.CPA_PERCENT, "0"),
                Arguments.of(PayoutModel.CPA_PERCENT, "-1"),
                Arguments.of(PayoutModel.CPA_PERCENT, "100.01"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "50", "100"})
    void shouldAcceptPercentInDomainRange(String value) {
        Campagne c = campagne(PayoutModel.CPA_PERCENT, value, START, END);
        assertEquals(0, c.getPayoutValue().compareTo(new BigDecimal(value)));
    }

    @Test
    void shouldRejectEndDateEqualToStartDate() {
        assertThrows(CampagneNotValidateException.class,
                () -> campagne(PayoutModel.CPC, "2.00", START, START));
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        assertThrows(CampagneNotValidateException.class,
                () -> campagne(PayoutModel.CPC, "2.00", START, START.minusDays(1)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void shouldRejectNonPositiveBudget(String budget) {
        assertThrows(CampagneNotValidateException.class,
                () -> new Campagne(offre(), "Campagne", START, END,
                        new BigDecimal(budget), PayoutModel.CPC, new BigDecimal("1")));
    }
}
