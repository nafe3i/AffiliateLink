package com.affiliatelink.profile.service;

import com.affiliatelink.common.exception.EmailAlreadyExistsException;
import com.affiliatelink.profile.dto.advertiserDto.AdvetiserResponseDTO;
import com.affiliatelink.profile.dto.advertiserDto.CreateAdvertiserRequestDto;
import com.affiliatelink.profile.entity.Advertiser;
import com.affiliatelink.profile.repository.AdvertiserRepository;
import com.affiliatelink.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdvertiserService {
    private AdvertiserRepository advertiserRepository;
    private ProfileRepository profileRepository;


    public AdvertiserService(AdvertiserRepository advertiserRepository
            , ProfileRepository profileRepository) {
        this.advertiserRepository = advertiserRepository;
        this.profileRepository = profileRepository;

    }

    @Transactional
    public AdvetiserResponseDTO createAdvertiser(CreateAdvertiserRequestDto advertiserRequestDto) {
        if (profileRepository.existsByEmail(advertiserRequestDto.email())) {
            throw new EmailAlreadyExistsException("Email already existe.");
        }
        Advertiser advertiser = new Advertiser(advertiserRequestDto.email(), advertiserRequestDto.companyName(), advertiserRequestDto.sector(), advertiserRequestDto.website());
        Advertiser advertiser1 = advertiserRepository.save(advertiser);

        return toResponseDTO(advertiser);

    }

    private AdvetiserResponseDTO toResponseDTO(Advertiser advertiser) {
        return new AdvetiserResponseDTO(advertiser.getId(),
                advertiser.getEmail(),
                advertiser.getCompanyName(),
                advertiser.getSector(),
                advertiser.getWebsite());
    }
}
