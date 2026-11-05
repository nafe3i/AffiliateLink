
package com.affiliatelink.profile.service;

import com.affiliatelink.common.exception.profil.ProfileNotFoudException;
import com.affiliatelink.profile.ProfileSuspended;
import com.affiliatelink.profile.dto.ProfileDTO;
import com.affiliatelink.profile.entity.Advertiser;
import com.affiliatelink.profile.entity.Profile;
import com.affiliatelink.profile.entity.enums.ProfileStatus;
import com.affiliatelink.profile.repository.ProfileRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class ProfileService {

    private static final Logger log =
            LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ProfileService(
            ProfileRepository profileRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.profileRepository = profileRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    // =====================================
    // CHANGEMENT DE STATUT D'UN PROFIL
    // =====================================

    @Transactional
    public ProfileDTO changeProfileStatus(Long idProfile) {

        log.debug(
                "Profile status change requested profileId={}",
                idProfile
        );

        Profile profile = profileRepository.findById(idProfile)
                .orElseThrow(() ->
                        new ProfileNotFoudException(
                                "Profile not found : " + idProfile
                        )
                );

        ProfileStatus ancienStatut = profile.getStatus();

        boolean isAdvertiser = profile instanceof Advertiser;
        boolean suspensionEventPublished = false;

        if (ancienStatut == ProfileStatus.ACTIVE) {

            // Transition ACTIVE -> SUSPENDED
            profile.suspend();

            // Les campagnes appartiennent aux advertisers.
            // Ne pas publier cet evenement pour un publisher.
            if (isAdvertiser) {

                applicationEventPublisher.publishEvent(
                        new ProfileSuspended(profile.getId())
                );

                suspensionEventPublished = true;

                log.debug(
                        "ProfileSuspended event published profileId={}",
                        idProfile
                );
            }

        } else {

            // Conserve le comportement actuel :
            // tout statut non ACTIVE est traite par activate().
            profile.activate();
        }

        ProfileStatus nouveauStatut = profile.getStatus();

        ProfileDTO response = new ProfileDTO(
                profile.getId(),
                profile.getEmail(),
                nouveauStatut
        );

        logStatusChangeAfterCommit(
                profile.getId(),
                ancienStatut,
                nouveauStatut,
                isAdvertiser,
                suspensionEventPublished
        );

        return response;
    }

    // =====================================
    // LOGGING APRES COMMIT
    // =====================================

    private void logStatusChangeAfterCommit(
            Long profileId,
            ProfileStatus ancienStatut,
            ProfileStatus nouveauStatut,
            boolean isAdvertiser,
            boolean suspensionEventPublished
    ) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        log.info(
                                "Profile status changed profileId={} type={} from={} to={}",
                                profileId,
                                isAdvertiser ? "ADVERTISER" : "OTHER",
                                ancienStatut,
                                nouveauStatut
                        );

                        if (suspensionEventPublished) {

                            log.info(
                                    "Advertiser suspension event published profileId={}",
                                    profileId
                            );
                        }
                    }
                }
        );
    }
}
