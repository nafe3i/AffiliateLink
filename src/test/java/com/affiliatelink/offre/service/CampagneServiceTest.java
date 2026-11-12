package com.affiliatelink.offre.service;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.offre.CampagneNotFoundException;
import com.affiliatelink.common.exception.offre.CampagneNotValidateException;
import com.affiliatelink.common.exception.offre.CampagneStatusConflictException;
import com.affiliatelink.offre.entity.Campagne;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.entity.enums.CampagneStatus;
import com.affiliatelink.offre.entity.enums.PayoutModel;
import com.affiliatelink.offre.mapper.CampagneMapper;
import com.affiliatelink.offre.repository.CampagneRepository;
import com.affiliatelink.offre.repository.OffreRepository;
import com.affiliatelink.profile.ProfileApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampagneServiceTest {
    @Mock
    private CampagneRepository campagneRepository;
    @Mock
    private OffreRepository offreRepository;
    @Mock
    private ProfileApi profileApi;
    @Mock
    private CampagneMapper campagneMapper;
    @InjectMocks
    private CampagneService campagneService;

    private static Offre offre(long id) {
        Offre offre = new Offre(1L, "Offre", "Description", "https://example.com", Niche.TECH);
        ReflectionTestUtils.setField(offre, "id", id);
        return offre;
    }

    private static Campagne campagne(Offre offre, long id) {
        Campagne c = new Campagne(offre, "Campagne " + id,
                LocalDate.of(2020, 1, 1), LocalDate.of(2100, 2, 1),
                new BigDecimal("100.00"), PayoutModel.CPC, new BigDecimal("1.00"));
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    @Test
    void shouldPausePreviousActiveCampaignWhenActivatingAnother() {
        // Given : deux campagnes de la même offre, l'une déjà ACTIVE.
        Offre offre = offre(11L);
        Campagne previous = campagne(offre, 21L);
        previous.activer(LocalDate.of(2026, 1, 2));
        Campagne next = campagne(offre, 22L);

        when(campagneRepository.findOffreIdByCampagneId(22L)).thenReturn(Optional.of(11L));
        when(offreRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(offre));
        when(campagneRepository.findById(22L)).thenReturn(Optional.of(next));
        when(campagneRepository.findByOffre_IdAndStatusAndIdNot(
                11L, CampagneStatus.ACTIVE, 22L)).thenReturn(List.of(previous));

        TransactionSynchronizationManager.initSynchronization();
        try {
            // When
            campagneService.changerStatut(22L, CampagneStatus.ACTIVE);

            // Then
            assertEquals(CampagneStatus.PAUSED, previous.getStatus());
            assertEquals(CampagneStatus.ACTIVE, next.getStatus());
            verify(profileApi).requiredActiveAdvertiser(1L);
            verify(campagneRepository).flush();
            verify(offreRepository).findByIdForUpdate(11L);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldRejectNullRequestedStatusBeforeDatabaseAccess() {
        assertThrows(CampagneNotValidateException.class,
                () -> campagneService.changerStatut(22L, null));
        verifyNoInteractions(campagneRepository, offreRepository, profileApi);
    }

    @Test
    void shouldRejectUnknownCampaign() {
        when(campagneRepository.findOffreIdByCampagneId(99L))
                .thenReturn(Optional.empty());
        assertThrows(CampagneNotFoundException.class,
                () -> campagneService.changerStatut(99L, CampagneStatus.ACTIVE));
        verifyNoInteractions(offreRepository, profileApi);
    }

    @Test
    void shouldPauseActiveCampaignsAfterAdvertiserSuspension() {
        // Deux offres différentes du même advertiser : chacune peut avoir
        // une campagne ACTIVE en respectant la contrainte d'unicité par offre.
        Campagne first = campagne(offre(11L), 30L);
        Campagne second = campagne(offre(12L), 31L);
        first.activer(LocalDate.of(2026, 1, 2));
        second.activer(LocalDate.of(2026, 1, 2));
        when(campagneRepository.findByOffre_AdvertiserIdAndStatus(
                1L, CampagneStatus.ACTIVE)).thenReturn(List.of(first, second));

        TransactionSynchronizationManager.initSynchronization();
        try {
            campagneService.mettreEnPauseCampagnesAnnonceur(1L);
            assertEquals(CampagneStatus.PAUSED, first.getStatus());
            assertEquals(CampagneStatus.PAUSED, second.getStatus());
            verify(campagneRepository).findByOffre_AdvertiserIdAndStatus(
                    1L, CampagneStatus.ACTIVE);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
