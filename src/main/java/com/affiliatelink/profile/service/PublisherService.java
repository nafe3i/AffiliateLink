package com.affiliatelink.profile.service;

import com.affiliatelink.profile.common.exception.EmailAlreadyExistsException;
import com.affiliatelink.profile.common.exception.PublisherNotFoudException;
import com.affiliatelink.profile.dto.publisherDto.CreatePublisherRequestDTO;
import com.affiliatelink.profile.dto.publisherDto.PublisherResponseDTO;
import com.affiliatelink.profile.entity.Publisher;
import com.affiliatelink.profile.repository.ProfileRepository;
import com.affiliatelink.profile.repository.PublisherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;


@Service
public class PublisherService {

    private PublisherRepository publisherRepository;
    private ProfileRepository profileRepository;

    public PublisherService(PublisherRepository publisherRepository, ProfileRepository profileRepository) {
        this.publisherRepository = publisherRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public PublisherResponseDTO createpublisher(CreatePublisherRequestDTO dtoRequest) {
        if (profileRepository.existsByEmail(dtoRequest.email())) {
            throw new EmailAlreadyExistsException("Email already existe.");
        }

        Publisher publisher = new Publisher(dtoRequest.email(), dtoRequest.firstName(), dtoRequest.lastName(), dtoRequest.niche(), dtoRequest.bio(), dtoRequest.country());


        Publisher publisher1 = publisherRepository.save(publisher);
        return toResponseDTO(publisher1);

    }

    public List<PublisherResponseDTO> getAllPublisher() {
        List<Publisher> publishersList = publisherRepository.findAll();
        return publishersList.stream().map(p -> toResponseDTO(p)).toList();
    }

    public PublisherResponseDTO getPublisher(Long idPublisher) {

        Optional<Publisher> publisher = publisherRepository.findById(idPublisher);
        if (publisher.isEmpty()) {
            throw new PublisherNotFoudException("Publisher not foud.");

        }
        return toResponseDTO(publisher.get());
//        return toResponseDTO(publisher);
    }

    private PublisherResponseDTO toResponseDTO(Publisher publisher) {
        return new PublisherResponseDTO(publisher.getId(),
                publisher.getEmail(),
                publisher.getFirstName(),
                publisher.getLastName(),
                publisher.getNiche(),
                publisher.getBio(),
                publisher.getCountry(),
                publisher.getStatus(),
                publisher.getCreatedAt())
                ;
    }
}
