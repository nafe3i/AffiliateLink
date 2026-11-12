
package com.affiliatelink.profile.service;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.profil.ProfileNotFoudException;
import com.affiliatelink.profile.ProfileSuspended;
import com.affiliatelink.profile.dto.ProfileDTO;
import com.affiliatelink.profile.entity.Advertiser;
import com.affiliatelink.profile.entity.Publisher;
import com.affiliatelink.profile.entity.enums.ProfileStatus;
import com.affiliatelink.profile.repository.ProfileRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ProfileService profileService;

    // --------------------------------------------------
    // Transitions de statut d'un Advertiser
    // --------------------------------------------------

    @ParameterizedTest(
            name = "{0} -> {1}, evenement attendu : {2}"
    )
    @MethodSource("advertiserTransitions")
    @DisplayName("Transitions de statut d'un Advertiser")
    void shouldApplyAdvertiserStatusTransition(
            ProfileStatus initialStatus,
            ProfileStatus expectedStatus,
            boolean eventExpected
    ) {

        // GIVEN
        Long id = 2L;

        Advertiser advertiser = new Advertiser(
                "advertiser@example.com",
                "YouCode",
                "TECH",
                "https://example.com"
        );

        ReflectionTestUtils.setField(advertiser, "id", id);

        // Préparer l'état initial
        switch (initialStatus) {
            case ACTIVE -> advertiser.activate();

            case SUSPENDED -> {
                advertiser.activate();
                advertiser.suspend();
            }

            case PENDING -> {
                // État initial du constructeur
            }
        }

        assertEquals(initialStatus, advertiser.getStatus());

        when(profileRepository.findById(id))
                .thenReturn(Optional.of(advertiser));

        TransactionSynchronizationManager.initSynchronization();

        try {

            // WHEN
            ProfileDTO result =
                    profileService.changeProfileStatus(id);

            // THEN : résultat et état
            assertNotNull(result);
            assertEquals(expectedStatus, result.status());
            assertEquals(expectedStatus, advertiser.getStatus());
            assertEquals(id, result.id());
            assertEquals(advertiser.getEmail(), result.email());

            // THEN : événement métier
            if (eventExpected) {
                verify(applicationEventPublisher)
                        .publishEvent(new ProfileSuspended(id));
            } else {
                verifyNoInteractions(applicationEventPublisher);
            }

        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    static Stream<Arguments> advertiserTransitions() {
        return Stream.of(
                Arguments.of(
                        ProfileStatus.PENDING,
                        ProfileStatus.ACTIVE,
                        false
                ),
                Arguments.of(
                        ProfileStatus.ACTIVE,
                        ProfileStatus.SUSPENDED,
                        true
                ),
                Arguments.of(
                        ProfileStatus.SUSPENDED,
                        ProfileStatus.ACTIVE,
                        false
                )
        );
    }

    // --------------------------------------------------
    // Publisher ACTIVE -> SUSPENDED
    // --------------------------------------------------

    @Test
    @DisplayName(
            "Un Publisher actif est suspendu sans événement ProfileSuspended"
    )
    void shouldSuspendActivePublisherWithoutPublishingEvent() {

        // GIVEN
        Long id = 4L;

        Publisher publisher = new Publisher(
                "publisher@example.com",
                "Ali",
                "Test",
                Niche.TECH,
                "Développement informatique",
                "Morocco"
        );

        ReflectionTestUtils.setField(publisher, "id", id);
        publisher.activate();

        when(profileRepository.findById(id))
                .thenReturn(Optional.of(publisher));

        TransactionSynchronizationManager.initSynchronization();

        try {

            // WHEN
            ProfileDTO result =
                    profileService.changeProfileStatus(id);

            // THEN
            assertEquals(ProfileStatus.SUSPENDED, result.status());
            assertEquals(ProfileStatus.SUSPENDED, publisher.getStatus());
            assertEquals(id, result.id());

            verifyNoInteractions(applicationEventPublisher);

        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // --------------------------------------------------
    // Profil inexistant
    // --------------------------------------------------

    @Test
    @DisplayName("Un profil inexistant déclenche ProfileNotFoudException")
    void shouldThrowWhenProfileNotFound() {

        // GIVEN
        Long idNotFound = 99L;

        when(profileRepository.findById(idNotFound))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(
                ProfileNotFoudException.class,
                () -> profileService.changeProfileStatus(idNotFound)
        );

        verify(profileRepository).findById(idNotFound);
        verifyNoInteractions(applicationEventPublisher);
    }
}
