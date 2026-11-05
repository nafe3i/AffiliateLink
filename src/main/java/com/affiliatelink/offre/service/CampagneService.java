
package com.affiliatelink.offre.service;

import com.affiliatelink.common.exception.offre.CampagneNotFoundException;
import com.affiliatelink.common.exception.offre.CampagneNotValidateException;
import com.affiliatelink.common.exception.offre.CampagneStatusConflictException;
import com.affiliatelink.common.exception.offre.OffreNotFoundException;

import com.affiliatelink.offre.CampaignInfo;
import com.affiliatelink.offre.OfferApi;
import com.affiliatelink.offre.dto.CampagneResponse;
import com.affiliatelink.offre.dto.CreateCampagneRequest;
import com.affiliatelink.offre.entity.Campagne;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.entity.enums.CampagneStatus;
import com.affiliatelink.offre.mapper.CampagneMapper;
import com.affiliatelink.offre.repository.CampagneRepository;
import com.affiliatelink.offre.repository.OffreRepository;
import com.affiliatelink.profile.ProfileApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CampagneService implements OfferApi {

    private static final Logger log =
            LoggerFactory.getLogger(CampagneService.class);

    private final CampagneMapper campagneMapper;
    private final CampagneRepository campagneRepository;
    private final OffreRepository offreRepository;
    private final ProfileApi profileApi;

    public CampagneService(
            CampagneRepository campagneRepository,
            OffreRepository offreRepository,
            ProfileApi profileApi,
            CampagneMapper campagneMapper
    ) {
        this.campagneRepository = campagneRepository;
        this.offreRepository = offreRepository;
        this.profileApi = profileApi;
        this.campagneMapper = campagneMapper;
    }

    // =====================================
    // CREATION D'UNE CAMPAGNE
    // =====================================

    @Transactional
    public CampagneResponse createCampagne(
            Long offreId,
            CreateCampagneRequest request
    ) {

        log.debug(
                "Campaign creation requested offerId={}",
                offreId
        );

        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() ->
                        new OffreNotFoundException(
                                "Offre introuvable : " + offreId
                        )
                );

        profileApi.requiredActiveAdvertiser(
                offre.getAdvertiserId()
        );

        Campagne campagne = new Campagne(
                offre,
                request.name(),
                request.startDate(),
                request.endDate(),
                request.budget(),
                request.payoutModel(),
                request.payoutValue()
        );

        Campagne savedCampagne =
                campagneRepository.save(campagne);

        CampagneResponse response =
                campagneMapper.toResponse(savedCampagne);

        Long campaignId = savedCampagne.getId();
        CampagneStatus status = savedCampagne.getStatus();

        logAfterCommit(() ->
                log.info(
                        "Campaign created campaignId={} offerId={} status={}",
                        campaignId,
                        offreId,
                        status
                )
        );

        return response;
    }

    // =====================================
    // CHANGEMENT DU STATUT D'UNE CAMPAGNE
    // =====================================

    @Transactional
    public void changerStatut(
            Long campagneId,
            CampagneStatus nouveauStatut
    ) {

        log.debug(
                "Campaign status change requested campaignId={} requestedStatus={}",
                campagneId,
                nouveauStatut
        );

        if (nouveauStatut == null) {
            throw new CampagneNotValidateException(
                    "Le statut est obligatoire"
            );
        }

        // Identifier l'offre de la campagne
        Long offreId = campagneRepository
                .findOffreIdByCampagneId(campagneId)
                .orElseThrow(() ->
                        new CampagneNotFoundException(
                                "Campagne introuvable : " + campagneId
                        )
                );

        // Verrouiller l'offre pour serialiser
        // les activations de ses campagnes
        Offre offre = offreRepository
                .findByIdForUpdate(offreId)
                .orElseThrow(() ->
                        new OffreNotFoundException(
                                "Offre introuvable : " + offreId
                        )
                );

        Campagne campagne = campagneRepository
                .findById(campagneId)
                .orElseThrow(() ->
                        new CampagneNotFoundException(
                                "Campagne introuvable : " + campagneId
                        )
                );

        CampagneStatus ancienStatut = campagne.getStatus();

        // Conserver les identifiants des campagnes
        // automatiquement mises en pause
        List<Long> campagnesPausees = new ArrayList<>();

        switch (nouveauStatut) {

            case ACTIVE -> {

                profileApi.requiredActiveAdvertiser(
                        offre.getAdvertiserId()
                );

                List<Campagne> campagnesActives =
                        campagneRepository
                                .findByOffre_IdAndStatusAndIdNot(
                                        offreId,
                                        CampagneStatus.ACTIVE,
                                        campagneId
                                );

                for (Campagne autre : campagnesActives) {

                    autre.mettreEnPause();
                    campagnesPausees.add(autre.getId());
                }

                // Les anciennes campagnes doivent
                // etre PAUSED en base avant l'activation,
                // a cause de l'index unique partiel V4.
                campagneRepository.flush();

                // Les validations de statut et de date
                // restent dans l'entite Campagne.
                campagne.activer(LocalDate.now());
            }

            case PAUSED -> campagne.mettreEnPause();

            case ENDED -> campagne.terminer();

            case DRAFT -> throw new CampagneStatusConflictException(
                    "Impossible de revenir au statut DRAFT"
            );
        }

        CampagneStatus statutFinal = campagne.getStatus();
        List<Long> idsPauses = List.copyOf(campagnesPausees);

        logAfterCommit(() -> {

            log.info(
                    "Campaign status changed campaignId={} offerId={} from={} to={}",
                    campagneId,
                    offreId,
                    ancienStatut,
                    statutFinal
            );

            for (Long idPause : idsPauses) {
                log.info(
                        "Campaign automatically paused campaignId={} offerId={} replacedByCampaignId={}",
                        idPause,
                        offreId,
                        campagneId
                );
            }
        });
    }

    // =====================================
    // LISTER LES CAMPAGNES
    // =====================================

    @Transactional(readOnly = true)
    public Page<CampagneResponse> listerCampagnes(
            CampagneStatus status,
            Pageable pageable
    ) {

        log.debug(
                "Listing campaigns statusFilter={} page={} size={}",
                status,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<Campagne> campagnes;

        if (status == null) {

            campagnes = campagneRepository.findAll(pageable);

        } else {

            campagnes = campagneRepository.findByStatus(
                    status,
                    pageable
            );
        }

        Page<CampagneResponse> response =
                campagnes.map(campagneMapper::toResponse);

        log.debug(
                "Campaign listing completed statusFilter={} totalElements={}",
                status,
                response.getTotalElements()
        );

        return response;
    }

    // =====================================
    // SUSPENSION AUTOMATIQUE PAR ANNONCEUR
    // =====================================

    @Transactional
    public void mettreEnPauseCampagnesAnnonceur(
            Long advertiserId
    ) {

        log.debug(
                "Advertiser campaign suspension requested advertiserId={}",
                advertiserId
        );

        List<Campagne> campagnesActives =
                campagneRepository
                        .findByOffre_AdvertiserIdAndStatus(
                                advertiserId,
                                CampagneStatus.ACTIVE
                        );

        List<Long> idsPauses = new ArrayList<>();

        for (Campagne campagne : campagnesActives) {

            campagne.mettreEnPause();
            idsPauses.add(campagne.getId());
        }

        List<Long> idsConfirmes = List.copyOf(idsPauses);

        logAfterCommit(() -> {

            log.info(
                    "Advertiser campaign suspension completed advertiserId={} pausedCount={}",
                    advertiserId,
                    idsConfirmes.size()
            );

            for (Long campagneId : idsConfirmes) {

                log.debug(
                        "Campaign paused following advertiser suspension campaignId={} advertiserId={}",
                        campagneId,
                        advertiserId
                );
            }
        });
    }

    // =====================================
    // API PUBLIQUE DU MODULE OFFRE
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public CampaignInfo getCampaign(Long campaignId) {

        log.debug(
                "Public OfferApi campaign lookup requested campaignId={}",
                campaignId
        );

        Campagne campagne =
                campagneRepository.findById(campaignId)
                        .orElseThrow(() ->
                                new CampagneNotFoundException(
                                        "Campagne introuvable : " + campaignId
                                )
                        );

        CampaignInfo response =
                campagneMapper.toCampaignInfo(campagne);

        log.debug(
                "Public OfferApi campaign lookup completed campaignId={}",
                campaignId
        );

        return response;
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
