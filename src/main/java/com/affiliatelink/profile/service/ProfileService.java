package com.affiliatelink.profile.service;

import com.affiliatelink.common.exception.ProfileNotFoudException;
import com.affiliatelink.profile.ProfileSuspended;
import com.affiliatelink.profile.dto.ProfileDTO;
import com.affiliatelink.profile.entity.Profile;
import com.affiliatelink.profile.entity.enums.ProfileStatus;
import com.affiliatelink.profile.repository.ProfileRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private ProfileRepository profileRepository;
    private ApplicationEventPublisher applicationEventPublisher;

    public ProfileService(ProfileRepository profileRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.profileRepository = profileRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public ProfileDTO changeProfileStatus(Long idProfile) {
        Profile profile = profileRepository.findById(idProfile).orElseThrow(() -> new ProfileNotFoudException("Profile not foud."));

        if (profile.getStatus().equals(ProfileStatus.ACTIVE)) {
            profile.suspend();
            applicationEventPublisher.publishEvent(new ProfileSuspended(profile.getId()));
        } else {
            profile.activate();

        }
        return new ProfileDTO(
                profile.getId(),
                profile.getEmail(),
                profile.getStatus()
        );

    }
}
