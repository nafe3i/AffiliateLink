package com.affiliatelink.offre.service;

import com.affiliatelink.common.exception.offre.CampagneNotFoundException;
import com.affiliatelink.common.exception.offre.CampagneNotValidateException;
import com.affiliatelink.common.exception.offre.CampagneStatusConflictException;
import com.affiliatelink.common.exception.offre.OffreNotFoundException;
import com.affiliatelink.offre.dto.CampagneResponse;
import com.affiliatelink.offre.dto.CreateCampagneRequest;
import com.affiliatelink.offre.entity.Campagne;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.entity.enums.CampagneStatus;
import com.affiliatelink.offre.mapper.CampagneMapper;
import com.affiliatelink.offre.repository.CampagneRepository;
import com.affiliatelink.offre.repository.OffreRepository;
import com.affiliatelink.profile.ProfileApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CampagneService {
    private final CampagneMapper campagneMapper;
    private final CampagneRepository campagneRepository;
    private final OffreRepository offreRepository;
    private final ProfileApi profileApi;

    public CampagneService(CampagneRepository campagneRepository, OffreRepository offreRepository, ProfileApi profileApi, CampagneMapper campagneMapper) {
        this.campagneRepository = campagneRepository;
        this.offreRepository = offreRepository;
        this.profileApi = profileApi;
        this.campagneMapper = campagneMapper;
    }

    @Transactional
    public CampagneResponse createCampagne(
            Long offreId,
            CreateCampagneRequest request
    ) {
        // 1. Vérifier que l'offre existe
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() ->
                        new OffreNotFoundException(
                                "Offre not found : " + offreId
                        )
                );

        // 2. Vérifier que l'annonceur est actif
        profileApi.requiredActiveAdvertiser(
                offre.getAdvertiserId()
        );

        // 3. Créer la campagne
        Campagne campagne = new Campagne(
                offre,
                request.name(),
                request.startDate(),
                request.endDate(),
                request.budget(),
                request.payoutModel(),
                request.payoutValue()
        );

        // 4. Enregistrer la campagne
        Campagne savedCampagne =
                campagneRepository.save(campagne);

        // 5. Convertir en DTO
        return campagneMapper.toResponse(savedCampagne);
    }

    @Transactional
    public void changerStatut(
            Long campagneId,
            CampagneStatus nouveauStatut
    ) {

        if (nouveauStatut == null) {
            throw new CampagneNotValidateException(
                    "Le statut est obligatoire"
            );
        }

        // 1. Identifier l'offre de cette campagne
        Long offreId = campagneRepository
                .findOffreIdByCampagneId(campagneId)
                .orElseThrow(() ->
                        new CampagneNotFoundException(
                                "Campagne introuvable : " + campagneId
                        )
                );

        // 2. Verrouiller l'offre
        Offre offre = offreRepository.findByIdForUpdate(offreId)
                .orElseThrow(() ->
                        new OffreNotFoundException(
                                "Offre introuvable : " + offreId
                        )
                );

        // 3. Charger la campagne
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() ->
                        new CampagneNotFoundException(
                                "Campagne introuvable : " + campagneId
                        )
                );

        // 4. Appliquer les règles métier
        switch (nouveauStatut) {

            case ACTIVE -> {

                // L'annonceur doit être actif
                profileApi.requiredActiveAdvertiser(
                        offre.getAdvertiserId()
                );

                // Rechercher les autres campagnes actives
                List<Campagne> campagnesActives =
                        campagneRepository.findByOffre_IdAndStatusAndIdNot(
                                offreId,
                                CampagneStatus.ACTIVE,
                                campagneId
                        );

                // Mettre les autres campagnes en pause
                for (Campagne autre : campagnesActives) {
                    autre.mettreEnPause();
                }

                // Synchroniser les pauses avec PostgreSQL
                campagneRepository.flush();

                // Activer la nouvelle campagne
                campagne.activer(LocalDate.now());
            }

            case PAUSED -> campagne.mettreEnPause();

            case ENDED -> campagne.terminer();

            case DRAFT -> throw new CampagneStatusConflictException(
                    "Impossible de revenir au statut DRAFT"
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<CampagneResponse> listerCampagnes(
            CampagneStatus status,
            Pageable pageable
    ) {
        Page<Campagne> campagnes;

        if (status == null) {
            campagnes = campagneRepository.findAll(pageable);
        } else {
            campagnes = campagneRepository.findByStatus(
                    status,
                    pageable
            );
        }

        return campagnes.map(campagneMapper::toResponse);
    }

    @Transactional
    public void mettreEnPauseCampagnesAnnonceur(Long advertiserId) {

        List<Campagne> campagnesActives =
                campagneRepository.findByOffre_AdvertiserIdAndStatus(
                        advertiserId,
                        CampagneStatus.ACTIVE
                );

        for (Campagne campagne : campagnesActives) {
            campagne.mettreEnPause();
        }
    }
}
