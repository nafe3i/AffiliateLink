package com.affiliatelink.offre.service;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.offre.OffreNotFoundException;
import com.affiliatelink.offre.dto.CreateOffreRequest;
import com.affiliatelink.offre.dto.OffreResponse;
import com.affiliatelink.offre.dto.UpdateOffreRequest;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.entity.Tag;
import com.affiliatelink.offre.repository.OffreRepository;
import com.affiliatelink.offre.repository.TagRepository;
import com.affiliatelink.offre.repository.specification.OffreSpecifications;
import com.affiliatelink.profile.ProfileApi;
import org.springframework.data.domain.Page;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class OffreService {

    private final OffreRepository offreRepository;
    private final TagRepository tagRepository;
    private final ProfileApi profileApi;

    public OffreService(OffreRepository offreRepository, TagRepository tagRepository, ProfileApi profileApi) {
        this.offreRepository = offreRepository;
        this.tagRepository = tagRepository;
        this.profileApi = profileApi;
    }

    @Transactional
    public OffreResponse creerOffre(CreateOffreRequest request) {

        profileApi.requiredActiveAdvertiser(request.advertiserId());

        Offre offre = new Offre(request.advertiserId(), request.title(), request.description(), request.landingUrl(), request.niche());

        for (String rawName : request.tags()) {

            Tag candidat = new Tag(rawName);

            Tag tag = tagRepository.findByName(candidat.getName()).orElseGet(() -> tagRepository.save(candidat));

            offre.ajouterTag(tag);
        }

        Offre saved = offreRepository.saveAndFlush(offre);
        return offreMapping(saved);
    }

//        List<String> tagNames = saved.getTags()
//                .stream()
//                .map(Tag::getName)
//                .sorted()
//                .toList();
//
//        return new OffreResponse(
//                saved.getId(),
//                saved.getAdvertiserId(),
//                saved.getTitle(),
//                saved.getDescription(),
//                saved.getLandingUrl(),
//                saved.getNiche(),
//                saved.getCreatedAt(),
//                tagNames
//        );

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

        Set<String> allowedSortFields = Set.of("id", "title", "createdAt");
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedSortFields.contains(order.getProperty())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Champ de tri non autorisé : " + order.getProperty()
                );
            }
        }
        Page<Offre> offres =
                offreRepository.findAll(specification, pageable);

        return offres.map(this::offreMapping);
    }
//
//        List<OffreResponse> offreResponses = offres.stream().filter(offre -> niche == null || offre.getNiche() == niche).
//                filter(offre ->
//                {
//                    System.out.printf("filtrage efectuer");
//                    return advertiserId == null || offre.getAdvertiserId().equals(advertiserId);
//                }).
//                filter(offre -> tagName == null || offre.getTags().stream().anyMatch(tag -> tag.getName().equals(tagName))).
//                filter(offre ->
//                        keyword == null ||
//                                offre.getTitle().toLowerCase(Locale.ROOT)
//                                        .contains(keyword.toLowerCase(Locale.ROOT))
//                ).
//                map(this::offreMapping).toList().sort(Comparator.comparing(Offre::getCreatedAt));


    @Transactional(readOnly = true)
    public OffreResponse getOffre(Long offreId) {

        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() ->
                        new OffreNotFoundException(
                                "Offre not found : " + offreId
                        )
                );

//        System.out.println(
//                "AVANT : " + offre.tagsSontInitialises()
//        );

        OffreResponse response = offreMapping(offre);

//        System.out.println(
//                "APRES : " + offre.tagsSontInitialises()
//        );

        return response;
    }

    @Transactional
    public OffreResponse modifierOffre(
            Long id,
            UpdateOffreRequest request
    ) {
        // 1. Rechercher l'offre


        Offre offre = offreRepository.findById(id)
                .orElseThrow(() ->
                        new OffreNotFoundException(
                                "Offre not found : " + id
                        )
                );

        // 2. Modifier ses informations
        offre.modifierInformations(request.title(), request.description(), request.landingUrl(), request.niche());
        // 3. Résoudre les tags en entités Tag
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
        // 4. Remplacer les associations
        offre.remplacerTags(nouveauxTags);
        // 5. Retourner le DTO
        return offreMapping(offre);
    }

    private OffreResponse offreMapping(Offre offre) {

        List<String> tagNames = offre.getTags()
                .stream()
                .map(Tag::getName)
                .sorted()
                .toList();

        return new OffreResponse(
                offre.getId(),
                offre.getAdvertiserId(),
                offre.getTitle(),
                offre.getDescription(),
                offre.getLandingUrl(),
                offre.getNiche(),
                offre.getCreatedAt(),
                tagNames
        );
    }


}
