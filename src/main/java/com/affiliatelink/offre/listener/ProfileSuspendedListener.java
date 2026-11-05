
package com.affiliatelink.offre.listener;

import com.affiliatelink.profile.ProfileSuspended;
import com.affiliatelink.offre.service.CampagneService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class ProfileSuspendedListener {

    private static final Logger log =
            LoggerFactory.getLogger(ProfileSuspendedListener.class);

    private final CampagneService campagneService;

    public ProfileSuspendedListener(
            CampagneService campagneService
    ) {
        this.campagneService = campagneService;
    }

    @ApplicationModuleListener
    public void on(ProfileSuspended event) {

        Long profileId = event.profileId();

        log.info(
                "ProfileSuspended event received profileId={}",
                profileId
        );

        try {

            // Mise en pause des campagnes actives
            // de l'annonceur suspendu.
            campagneService.mettreEnPauseCampagnesAnnonceur(
                    profileId
            );

            // Le listener utilise une transaction.
            // Confirmer le succes uniquement apres commit.
            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {

                                @Override
                                public void afterCommit() {

                                    log.info(
                                            "ProfileSuspended processing committed profileId={}",
                                            profileId
                                    );
                                }
                            }
                    );

        } catch (RuntimeException exception) {

            log.error(
                    "ProfileSuspended processing failed profileId={}",
                    profileId,
                    exception
            );

            // Ne pas masquer l'erreur :
            // Spring Modulith doit pouvoir suivre l'echec.
            throw exception;
        }
    }
}
