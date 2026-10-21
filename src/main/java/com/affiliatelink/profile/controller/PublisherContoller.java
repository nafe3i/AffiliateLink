package com.affiliatelink.profile.controller;

import com.affiliatelink.profile.dto.publisherDto.CreatePublisherRequestDTO;
import com.affiliatelink.profile.dto.publisherDto.PublisherResponseDTO;
import com.affiliatelink.profile.service.PublisherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publishers")
public class PublisherContoller {

    private PublisherService publisherService;

    public PublisherContoller(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @PostMapping()
    public ResponseEntity<PublisherResponseDTO> createPublisher(@Valid @RequestBody CreatePublisherRequestDTO createDto) {

        PublisherResponseDTO publisherResponseDTO = publisherService.createpublisher(createDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(publisherResponseDTO);
    }
}
