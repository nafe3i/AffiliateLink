package com.affiliatelink.offre.service;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.offre.OffreNotFoundException;
import com.affiliatelink.offre.dto.CreateOffreRequest;
import com.affiliatelink.offre.dto.OffreResponse;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.entity.Tag;
import com.affiliatelink.offre.repository.OffreRepository;
import com.affiliatelink.offre.repository.TagRepository;
import com.affiliatelink.profile.ProfileApi;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
    public List<OffreResponse> getAllOffres(Niche niche, Long advertiserId, String tagName, String keyword) {

        List<Offre> offres = offreRepository.findAll();

        List<OffreResponse> offreResponses = offres.stream().filter(offre -> niche == null || offre.getNiche() == niche).
                filter(offre ->
                {
                    System.out.printf("filtrage efectuer");
                    return advertiserId == null || offre.getAdvertiserId().equals(advertiserId);
                }).
                filter(offre -> tagName == null || offre.getTags().stream().anyMatch(tag -> tag.getName().equals(tagName))).
                filter(offre ->
                        keyword == null ||
                                offre.getTitle().toLowerCase(Locale.ROOT)
                                        .contains(keyword.toLowerCase(Locale.ROOT))
                ).
                map(this::offreMapping).toList().sort(Comparator.comparing(Offre::getCreatedAt));
        return offreResponses;
    }


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
