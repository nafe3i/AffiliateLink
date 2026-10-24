package com.affiliatelink.profile.service;

import com.affiliatelink.common.exception.ProfileNotFoudException;
import com.affiliatelink.profile.dto.ProfileDTO;
import com.affiliatelink.profile.entity.Profile;
import com.affiliatelink.profile.entity.enums.ProfileStatus;
import com.affiliatelink.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {

    private ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public ProfileDTO changeProfileStatus(Long idProfile) {
        Profile profile = profileRepository.findById(idProfile).orElseThrow(() -> new ProfileNotFoudException("Profile not foud."));

        if (profile.getStatus().equals(ProfileStatus.ACTIVE)) {
            profile.suspend();
        } else {
            profile.activate();

        }
        return new ProfileDTO(profile.getId(), profile.getEmail(), profile.getStatus());

    }
}
