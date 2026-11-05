package com.affiliatelink.offre.listener;

import com.affiliatelink.profile.ProfileSuspended;
import com.affiliatelink.offre.service.CampagneService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class ProfileSuspendedListener {

    private final CampagneService campagneService;

    public ProfileSuspendedListener(CampagneService campagneService) {
        this.campagneService = campagneService;
    }

    @ApplicationModuleListener
    public void on(ProfileSuspended event) {
        campagneService.mettreEnPauseCampagnesAnnonceur(
                event.profileId()
        );
    }
}