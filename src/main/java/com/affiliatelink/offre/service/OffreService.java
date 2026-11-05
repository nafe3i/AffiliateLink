
package com.affiliatelink.offre.service;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.offre.OffreNotFoundException;
import com.affiliatelink.offre.dto.*;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.entity.Tag;
import com.affiliatelink.offre.mapper.CampagneMapper;
import com.affiliatelink.offre.mapper.OffreMapping;
import com.affiliatelink.offre.repository.CampagneRepository;
import com.affiliatelink.offre.repository.OffreRepository;
import com.affiliatelink.offre.repository.TagRepository;
import com.affiliatelink.offre.repository.specification.OffreSpecifications;
import com.affiliatelink.profile.ProfileApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class OffreService {

    private static final Logger log =
            LoggerFactory.getLogger(OffreService.class);

    private final OffreRepository offreRepository;
    private final TagRepository tagRepository;
    private final ProfileApi profileApi;
    private final CampagneMapper campagneMapper;
    private final CampagneRepository campagneRepository;
    private final OffreMapping offreMapping;

    public OffreService(
            OffreRepository offreRepository,
            TagRepository tagRepository,
            ProfileApi profileApi,
            CampagneMapper campagneMapper,
            CampagneRepository campagneRepository,
            OffreMapping offreMapping
    ) {
        this.offreRepository = offreRepository;
        this.tagRepository = tagRepository;
        this.profileApi = profileApi;
        this.campagneMapper = campagneMapper;
        this.campagneRepository = campagneRepository;
        this.offreMapping = offreMapping;
    }

    // ============================
    // CREATION D'UNE OFFRE
    // ============================

    @Transactional
    public OffreResponse creerOffre(CreateOffreRequest request) {

        log.debug(
                "Offer creation requested advertiserId={}",
                request.advertiserId()
        );

        profileApi.requiredActiveAdvertiser(request.advertiserId());

        Offre offre = new Offre(
                request.advertiserId(),
                request.title(),
                request.description(),
                request.landingUrl(),
                request.niche()
        );

        for (String rawName : request.tags()) {

            Tag candidat = new Tag(rawName);

            Tag tag = tagRepository.findByName(candidat.getName())
                    .orElseGet(() -> tagRepository.save(candidat));

            offre.ajouterTag(tag);
        }

        Offre saved = offreRepository.saveAndFlush(offre);

        OffreResponse response = offreMapping.toResponse(saved);

        logMutationAfterCommit(
                "created",
                saved.getId(),
                saved.getAdvertiserId()
        );

        return response;
    }

    // ============================
    // RECHERCHE DES OFFRES
    // ============================

    @Transactional(readOnly = true)
    public Page<OffreResponse> getAllOffres(
            Niche niche,
            Long advertiserId,
            String tagName,
            String keyword,
            Pageable pageable
    ) {

        Specification<Offre> specification =
                OffreSpecifications.filtrer(
                        niche,
                        advertiserId,
                        tagName,
                        keyword
                );

        Set<String> allowedSortFields =
                Set.of("id", "title", "createdAt");

        for (Sort.Order order : pageable.getSort()) {
            if (!allowedSortFields.contains(order.getProperty())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Champ de tri non autorisé : "
                                + order.getProperty()
                );
            }
        }

        log.debug(
                "Searching offers niche={} advertiserFilter={} tagFilter={} keywordFilter={}",
                niche,
                advertiserId != null,
                tagName != null && !tagName.isBlank(),
                keyword != null && !keyword.isBlank()
        );

        Page<Offre> offres =
                offreRepository.findAll(specification, pageable);

        log.debug(
                "Offer search completed totalElements={}",
                offres.getTotalElements()
        );

        return offres.map(offreMapping::toResponse);
    }

    // ============================
    // CONSULTATION D'UNE OFFRE
    // ============================

    @Transactional(readOnly = true)
    public OffreResponse getOffre(Long offreId) {

        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() ->
                        new OffreNotFoundException(
                                "Offre introuvable : " + offreId
                        )
                );

        log.debug(
                "Offer retrieved offerId={}",
                offreId
        );

        return offreMapping.toResponse(offre);
    }

    // ============================
    // MODIFICATION D'UNE OFFRE
    // ============================

    @Transactional
    public OffreResponse modifierOffre(
            Long id,
            UpdateOffreRequest request
    ) {

        log.debug(
                "Offer update requested offerId={}",
                id
        );

        Offre offre = offreRepository.findById(id)
                .orElseThrow(() ->
                        new OffreNotFoundException(
                                "Offre introuvable : " + id
                        )
                );

        offre.modifierInformations(
                request.title(),
                request.description(),
                request.landingUrl(),
                request.niche()
        );

        Set<Tag> nouveauxTags = new HashSet<>();

        for (String tagName : request.tags()) {

            String normalizedName =
                    tagName.strip().toLowerCase(Locale.ROOT);

            Tag tag = tagRepository.findByName(normalizedName)
                    .orElseGet(() ->
                            tagRepository.save(new Tag(normalizedName))
                    );

            nouveauxTags.add(tag);
        }

        offre.remplacerTags(nouveauxTags);

        OffreResponse response = offreMapping.toResponse(offre);

        logMutationAfterCommit(
                "updated",
                offre.getId(),
                offre.getAdvertiserId()
        );

        return response;
    }

    // ============================
    // OFFRE AVEC SES CAMPAGNES
    // ============================

    @Transactional(readOnly = true)
    public OffreDetailResponse consulterOffreAvecCampagnes(Long id) {

        Offre offre = offreRepository.findById(id)
                .orElseThrow(() ->
                        new OffreNotFoundException(
                                "Offre introuvable : " + id
                        )
                );

        List<CampagneResponse> campagnes =
                campagneMapper.toResponses(
                        campagneRepository.findByOffre_IdOrderByIdDesc(id)
                );

        log.debug(
                "Offer details retrieved offerId={} campaignCount={}",
                id,
                campagnes.size()
        );

        return offreMapping.toDetailResponse(offre, campagnes);
    }

    // ============================
    // LOGGING APRES COMMIT
    // ============================

    private void logMutationAfterCommit(
            String action,
            Long offreId,
            Long advertiserId
    ) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        log.info(
                                "Offer {} offerId={} advertiserId={}",
                                action,
                                offreId,
                                advertiserId
                        );
                    }
                }
        );
    }
}
