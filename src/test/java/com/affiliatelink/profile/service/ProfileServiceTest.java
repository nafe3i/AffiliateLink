package com.affiliatelink.profile.service;


import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.profil.ProfileNotFoudException;
import com.affiliatelink.profile.ProfileSuspended;
import com.affiliatelink.profile.dto.ProfileDTO;
import com.affiliatelink.profile.entity.Advertiser;
import com.affiliatelink.profile.entity.Publisher;
import com.affiliatelink.profile.entity.enums.ProfileStatus;
import com.affiliatelink.profile.repository.ProfileRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ProfileService profileService;

    @ParameterizedTest(name = "{0} -> {1} ; événement attendu : {2}")
    @MethodSource("advertiserTransitions")
    @DisplayName("Transitions d'état d'un Advertiser et événements associés")
    void shouldApplyAdvertiserStatusTransition(
            ProfileStatus initialStatus,
            ProfileStatus expectedStatus,
            boolean eventExpected
    ) {
        // GIVEN
        Long id = 2L;
        Advertiser advertiser = new Advertiser(
                "advertiser@example.com", "YouCode", "TECH", "https://example.com"
        );
        ReflectionTestUtils.setField(advertiser, "id", id);

        switch (initialStatus) {
            case PENDING -> { /* statut par défaut */ }
            case ACTIVE -> advertiser.activate();
            case SUSPENDED -> {
                advertiser.activate();
                advertiser.suspend();
            }
        }
        Assertions.assertEquals(initialStatus, advertiser.getStatus());
        Mockito.when(profileRepository.findById(id)).thenReturn(Optional.of(advertiser));

        // Le service enregistre un TransactionSynchronization : ceci n'est PAS un vrai commit.
        TransactionSynchronizationManager.initSynchronization();
        try {
            // WHEN
            ProfileDTO actual = profileService.changeProfileStatus(id);

            // THEN
            Assertions.assertEquals(expectedStatus, actual.status());
            Assertions.assertEquals(expectedStatus, advertiser.getStatus());
            Assertions.assertEquals(id, actual.id());
            Assertions.assertEquals(advertiser.getEmail(), actual.email());

            if (eventExpected) {
                Mockito.verify(applicationEventPublisher).publishEvent(new ProfileSuspended(id));
            } else {
                Mockito.verifyNoInteractions(applicationEventPublisher);
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    static Stream<Arguments> advertiserTransitions() {
        return Stream.of(
                Arguments.of(ProfileStatus.PENDING, ProfileStatus.ACTIVE, false),
                Arguments.of(ProfileStatus.ACTIVE, ProfileStatus.SUSPENDED, true),
                Arguments.of(ProfileStatus.SUSPENDED, ProfileStatus.ACTIVE, false)
        );
    }

    @Test
    @DisplayName("La suspension d'un Publisher ne publie pas ProfileSuspended")
    void shouldSuspendActivePublisherWithoutPublishingEvent() {
        // GIVEN
        Long id = 4L;
        Publisher publisher = new Publisher(
                "publisher@example.com", "Ali", "Amine", Niche.TECH,
                "Développement informatique", "Morocco"
        );
        ReflectionTestUtils.setField(publisher, "id", id);
        publisher.activate();
        Mockito.when(profileRepository.findById(id)).thenReturn(Optional.of(publisher));

        TransactionSynchronizationManager.initSynchronization();
        try {
            // WHEN
            ProfileDTO result = profileService.changeProfileStatus(id);

            // THEN
            Assertions.assertEquals(ProfileStatus.SUSPENDED, result.status());
            Assertions.assertEquals(ProfileStatus.SUSPENDED, publisher.getStatus());
            Assertions.assertEquals(id, result.id());
            Assertions.assertEquals(publisher.getEmail(), result.email());
            Mockito.verifyNoInteractions(applicationEventPublisher);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("Un profil absent provoque ProfileNotFoudException sans événement")
    void shouldThrowWhenProfileNotFound() {
        // GIVEN
        Long absentId = 99L;
        Mockito.when(profileRepository.findById(absentId)).thenReturn(Optional.empty());

        // WHEN / THEN
        Assertions.assertThrows(
                ProfileNotFoudException.class,
                () -> profileService.changeProfileStatus(absentId)
        );
        Mockito.verifyNoInteractions(applicationEventPublisher);
    }
}
