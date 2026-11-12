package com.affiliatelink.profile.service;

import com.affiliatelink.common.exception.offre.StatusNotActiveException;
import com.affiliatelink.common.exception.profil.EmailAlreadyExistsException;
import com.affiliatelink.common.exception.profil.ProfileNotFoudException;
import com.affiliatelink.profile.dto.advertiserDto.AdvetiserResponseDTO;
import com.affiliatelink.profile.dto.advertiserDto.CreateAdvertiserRequestDto;
import com.affiliatelink.profile.entity.Advertiser;
import com.affiliatelink.profile.entity.enums.ProfileStatus;
import com.affiliatelink.profile.mapper.AdvertiserMapper;
import com.affiliatelink.profile.repository.AdvertiserRepository;
import com.affiliatelink.profile.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertiserServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private AdvertiserRepository advertiserRepository;
    @Mock
    private AdvertiserMapper advertiserMapper;
    @InjectMocks
    private AdvertiserService advertiserService;

    private static CreateAdvertiserRequestDto request() {
        return new CreateAdvertiserRequestDto(
                "advertiser@example.com", "YouCode", "TECH", "https://example.com");
    }

    private static Advertiser advertiser(Long id) {
        Advertiser advertiser = new Advertiser(
                "advertiser@example.com", "YouCode", "TECH", "https://example.com");
        ReflectionTestUtils.setField(advertiser, "id", id);
        return advertiser;
    }

    @Test
    @DisplayName("Un email dupliqué bloque l'inscription sans sauvegarde")
    void shouldRejectDuplicateEmailWithoutSavingAdvertiser() {
        // Given
        CreateAdvertiserRequestDto request = request();
        when(profileRepository.existsByEmail(request.email())).thenReturn(true);

        // When / Then
        assertThrows(EmailAlreadyExistsException.class,
                () -> advertiserService.createAdvertiser(request));
        verifyNoInteractions(advertiserRepository, advertiserMapper);
    }

    @Test
    @DisplayName("La création retourne le DTO et sauvegarde un Advertiser PENDING")
    void shouldCreateAdvertiserWhenEmailIsAvailable() {
        // Given
        CreateAdvertiserRequestDto request = request();
        Advertiser savedAdvertiser = advertiser(5L);
        AdvetiserResponseDTO expected = new AdvetiserResponseDTO(
                5L, request.email(), request.companyName(), request.sector(),
                request.website(), ProfileStatus.PENDING);
        when(advertiserRepository.save(any(Advertiser.class))).thenReturn(savedAdvertiser);
        when(advertiserMapper.toResponse(savedAdvertiser)).thenReturn(expected);
        // existsByEmail() vaut false par défaut avec Mockito : pas de stub inutile.
        TransactionSynchronizationManager.initSynchronization();
        try {
            // When
            AdvetiserResponseDTO result = advertiserService.createAdvertiser(request);

            // Then
            assertEquals(expected, result);
            ArgumentCaptor<Advertiser> captor = ArgumentCaptor.forClass(Advertiser.class);
            verify(advertiserRepository).save(captor.capture());
            Advertiser toSave = captor.getValue();
            assertAll(
                    () -> assertEquals(request.email(), toSave.getEmail()),
                    () -> assertEquals(request.companyName(), toSave.getCompanyName()),
                    () -> assertEquals(request.sector(), toSave.getSector()),
                    () -> assertEquals(request.website(), toSave.getWebsite()),
                    () -> assertEquals(ProfileStatus.PENDING, toSave.getStatus())
            );
            verify(advertiserMapper).toResponse(savedAdvertiser);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("Un Advertiser inexistant ne peut pas être consulté")
    void shouldThrowWhenAdvertiserNotFound() {
        when(advertiserRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ProfileNotFoudException.class, () -> advertiserService.getAdvertiser(99L));
        verifyNoInteractions(advertiserMapper);
    }

    @ParameterizedTest(name = "identifiant invalide = {0}")
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void shouldRejectInvalidAdvertiserIdForLookup(Long id) {
        assertThrows(ProfileNotFoudException.class, () -> advertiserService.getAdvertiser(id));
        verifyNoInteractions(advertiserRepository, advertiserMapper);
    }

    @Test
    @DisplayName("L'API Profile accepte un Advertiser actif")
    void shouldAllowActiveAdvertiser() {
        Advertiser advertiser = advertiser(10L);
        advertiser.activate();
        when(advertiserRepository.findById(10L)).thenReturn(Optional.of(advertiser));
        assertDoesNotThrow(() -> advertiserService.requiredActiveAdvertiser(10L));
        verifyNoInteractions(advertiserMapper);
    }

    @ParameterizedTest(name = "statut refusé = {0}")
    @EnumSource(value = ProfileStatus.class, names = {"PENDING", "SUSPENDED"})
    void shouldRejectNonActiveAdvertiser(ProfileStatus status) {
        Advertiser advertiser = advertiser(11L);
        if (status == ProfileStatus.SUSPENDED) {
            advertiser.activate();
            advertiser.suspend();
        }
        when(advertiserRepository.findById(11L)).thenReturn(Optional.of(advertiser));
        assertThrows(StatusNotActiveException.class,
                () -> advertiserService.requiredActiveAdvertiser(11L));
    }

    @Test
    void shouldRejectUnknownAdvertiserForActiveCheck() {
        when(advertiserRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ProfileNotFoudException.class,
                () -> advertiserService.requiredActiveAdvertiser(99L));
    }

    @ParameterizedTest(name = "identifiant invalide = {0}")
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void shouldRejectInvalidAdvertiserIdForActiveCheck(Long id) {
        assertThrows(ProfileNotFoudException.class,
                () -> advertiserService.requiredActiveAdvertiser(id));
        verifyNoInteractions(advertiserRepository);
    }
}
