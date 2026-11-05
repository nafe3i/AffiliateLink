
package com.affiliatelink.profile.service;

import com.affiliatelink.common.exception.profil.EmailAlreadyExistsException;
import com.affiliatelink.common.exception.profil.ProfileNotFoudException;

import com.affiliatelink.profile.dto.publisherDto.CreatePublisherRequestDTO;
import com.affiliatelink.profile.dto.publisherDto.PublisherResponseDTO;
import com.affiliatelink.profile.entity.Publisher;
import com.affiliatelink.profile.mapper.PublisherMapper;
import com.affiliatelink.profile.repository.ProfileRepository;
import com.affiliatelink.profile.repository.PublisherRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
public class PublisherService {

    private static final Logger log =
            LoggerFactory.getLogger(PublisherService.class);

    private final PublisherRepository publisherRepository;
    private final ProfileRepository profileRepository;
    private final PublisherMapper publisherMapper;

    public PublisherService(
            PublisherRepository publisherRepository,
            ProfileRepository profileRepository,
            PublisherMapper publisherMapper
    ) {
        this.publisherRepository = publisherRepository;
        this.profileRepository = profileRepository;
        this.publisherMapper = publisherMapper;
    }

    // =====================================
    // CREATION D'UN PUBLISHER
    // =====================================

    @Transactional
    public PublisherResponseDTO createpublisher(
            CreatePublisherRequestDTO dtoRequest
    ) {

        log.debug("Publisher creation requested");

        // 1. Verifier que l'email n'existe pas
        if (profileRepository.existsByEmail(dtoRequest.email())) {
            throw new EmailAlreadyExistsException(
                    "Email already exists."
            );
        }

        // 2. Creer l'entite avec ses regles metier
        Publisher publisher = new Publisher(
                dtoRequest.email(),
                dtoRequest.firstName(),
                dtoRequest.lastName(),
                dtoRequest.niche(),
                dtoRequest.bio(),
                dtoRequest.country()
        );

        // 3. Enregistrer
        Publisher savedPublisher =
                publisherRepository.save(publisher);

        // 4. Convertir avec MapStruct
        PublisherResponseDTO response =
                publisherMapper.toResponse(savedPublisher);

        // 5. Conserver les informations du log
        Long publisherId = savedPublisher.getId();
        var status = savedPublisher.getStatus();

        // 6. Journaliser uniquement apres commit
        logAfterCommit(() ->
                log.info(
                        "Publisher created publisherId={} status={}",
                        publisherId,
                        status
                )
        );

        return response;
    }

    // =====================================
    // LISTER LES PUBLISHERS
    // =====================================

    @Transactional(readOnly = true)
    public List<PublisherResponseDTO> getAllPublisher() {

        log.debug("Publisher listing requested");

        List<Publisher> publishers =
                publisherRepository.findAll();

        List<PublisherResponseDTO> response =
                publisherMapper.toResponses(publishers);

        log.debug(
                "Publisher listing completed count={}",
                response.size()
        );

        return response;
    }

    // =====================================
    // CONSULTER UN PUBLISHER
    // =====================================

    @Transactional(readOnly = true)
    public PublisherResponseDTO getPublisher(Long idPublisher) {

        log.debug(
                "Publisher lookup requested publisherId={}",
                idPublisher
        );

        if (idPublisher == null || idPublisher <= 0) {
            throw new ProfileNotFoudException(
                    "Publisher not found : " + idPublisher
            );
        }

        Publisher publisher = publisherRepository
                .findById(idPublisher)
                .orElseThrow(() ->
                        new ProfileNotFoudException(
                                "Publisher not found : " + idPublisher
                        )
                );

        PublisherResponseDTO response =
                publisherMapper.toResponse(publisher);

        log.debug(
                "Publisher lookup completed publisherId={}",
                idPublisher
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
