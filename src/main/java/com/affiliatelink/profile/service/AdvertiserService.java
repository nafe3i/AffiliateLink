
package com.affiliatelink.profile.service;

import com.affiliatelink.common.exception.profil.EmailAlreadyExistsException;
import com.affiliatelink.common.exception.profil.ProfileNotFoudException;
import com.affiliatelink.common.exception.offre.StatusNotActiveException;

import com.affiliatelink.profile.ProfileApi;
import com.affiliatelink.profile.dto.advertiserDto.AdvetiserResponseDTO;
import com.affiliatelink.profile.dto.advertiserDto.CreateAdvertiserRequestDto;
import com.affiliatelink.profile.entity.Advertiser;
import com.affiliatelink.profile.entity.enums.ProfileStatus;
import com.affiliatelink.profile.mapper.AdvertiserMapper;
import com.affiliatelink.profile.repository.AdvertiserRepository;
import com.affiliatelink.profile.repository.ProfileRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
public class AdvertiserService implements ProfileApi {

    private static final Logger log =
            LoggerFactory.getLogger(AdvertiserService.class);

    private final AdvertiserRepository advertiserRepository;
    private final ProfileRepository profileRepository;
    private final AdvertiserMapper advertiserMapper;

    public AdvertiserService(
            AdvertiserRepository advertiserRepository,
            ProfileRepository profileRepository,
            AdvertiserMapper advertiserMapper
    ) {
        this.advertiserRepository = advertiserRepository;
        this.profileRepository = profileRepository;
        this.advertiserMapper = advertiserMapper;
    }

    // =====================================
    // CREATION D'UN ADVERTISER
    // =====================================

    @Transactional
    public AdvetiserResponseDTO createAdvertiser(
            CreateAdvertiserRequestDto request
    ) {

        log.debug("Advertiser creation requested");

        if (profileRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(
                    "Email already exists."
            );
        }

        Advertiser advertiser = new Advertiser(
                request.email(),
                request.companyName(),
                request.sector(),
                request.website()
        );

        Advertiser savedAdvertiser =
                advertiserRepository.save(advertiser);

        AdvetiserResponseDTO response =
                advertiserMapper.toResponse(savedAdvertiser);

        Long advertiserId = savedAdvertiser.getId();

        logAfterCommit(() ->
                log.info(
                        "Advertiser created advertiserId={} status={}",
                        advertiserId,
                        savedAdvertiser.getStatus()
                )
        );

        return response;
    }

    // =====================================
    // LISTER LES ADVERTISERS
    // =====================================

    @Transactional(readOnly = true)
    public List<AdvetiserResponseDTO> getAllAdvertisers() {

        log.debug("Listing advertisers");

        List<Advertiser> advertisers =
                advertiserRepository.findAll();

        List<AdvetiserResponseDTO> response =
                advertiserMapper.toResponses(advertisers);

        log.debug(
                "Advertiser listing completed count={}",
                response.size()
        );

        return response;
    }

    // =====================================
    // CONSULTER UN ADVERTISER
    // =====================================

    @Transactional(readOnly = true)
    public AdvetiserResponseDTO getAdvertiser(Long idAdvertiser) {

        log.debug(
                "Advertiser lookup requested advertiserId={}",
                idAdvertiser
        );

        if (idAdvertiser == null || idAdvertiser <= 0) {
            throw new ProfileNotFoudException(
                    "Advertiser not found : " + idAdvertiser
            );
        }

        Advertiser advertiser = advertiserRepository
                .findById(idAdvertiser)
                .orElseThrow(() ->
                        new ProfileNotFoudException(
                                "Advertiser not found : " + idAdvertiser
                        )
                );

        AdvetiserResponseDTO response =
                advertiserMapper.toResponse(advertiser);

        log.debug(
                "Advertiser lookup completed advertiserId={}",
                idAdvertiser
        );

        return response;
    }

    // =====================================
    // API PUBLIQUE DU MODULE PROFILE
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public void requiredActiveAdvertiser(Long advertiserId) {

        log.debug(
                "Checking active advertiser advertiserId={}",
                advertiserId
        );

        if (advertiserId == null || advertiserId <= 0) {
            throw new ProfileNotFoudException(
                    "Advertiser not found : " + advertiserId
            );
        }

        Advertiser advertiser = advertiserRepository
                .findById(advertiserId)
                .orElseThrow(() ->
                        new ProfileNotFoudException(
                                "Advertiser not found : " + advertiserId
                        )
                );

        if (advertiser.getStatus() != ProfileStatus.ACTIVE) {
            throw new StatusNotActiveException(
                    "Advertiser not active"
            );
        }

        log.debug(
                "Active advertiser verified advertiserId={}",
                advertiserId
        );
    }

    // =====================================
    // LOGGING APRES COMMIT
    // =====================================

    private void logAfterCommit(Runnable action) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                }
        );
    }
}
