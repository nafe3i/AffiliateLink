package com.affiliatelink.profile.service;

import com.affiliatelink.common.exception.EmailAlreadyExistsException;
import com.affiliatelink.common.exception.ProfileNotFoudException;
import com.affiliatelink.common.exception.StatusNotActiveException;
import com.affiliatelink.profile.ProfileApi;
import com.affiliatelink.profile.dto.advertiserDto.AdvetiserResponseDTO;
import com.affiliatelink.profile.dto.advertiserDto.CreateAdvertiserRequestDto;
import com.affiliatelink.profile.entity.Advertiser;
import com.affiliatelink.profile.entity.Publisher;
import com.affiliatelink.profile.entity.enums.ProfileStatus;
import com.affiliatelink.profile.repository.AdvertiserRepository;
import com.affiliatelink.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdvertiserService implements ProfileApi {
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

    public List<AdvetiserResponseDTO> getAllAdvertisers() {

        List<Advertiser> advertiserList = advertiserRepository.findAll();
        return advertiserList.stream().map(adveriser -> toResponseDTO(adveriser)).toList();
    }

    public AdvetiserResponseDTO getAdvertiser(Long idAdvertiser) {
        Optional<Advertiser> advertiser = advertiserRepository.findById(idAdvertiser);
        if (advertiser.isEmpty()) {
            throw new ProfileNotFoudException("Advertiser not found");
        }
        return toResponseDTO(advertiser.get());
    }

    public void requiredActiveAdvertiser(Long advertiserId) {
        Optional<Advertiser> advertiser = advertiserRepository.findById(advertiserId);
        if (advertiser.isEmpty()) {
            throw new ProfileNotFoudException("Advertiser out found");
        }
        Advertiser advertiser1 = advertiser.get();
        if (advertiser1.getStatus() != ProfileStatus.ACTIVE) {
            throw new StatusNotActiveException("Advertiser not active");
        }
    }

    private AdvetiserResponseDTO toResponseDTO(Advertiser advertiser) {
        return new AdvetiserResponseDTO(advertiser.getId(),
                advertiser.getEmail(),
                advertiser.getCompanyName(),
                advertiser.getSector(),
                advertiser.getWebsite());
    }


}
