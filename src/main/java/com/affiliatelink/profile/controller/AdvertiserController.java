package com.affiliatelink.profile.controller;


import com.affiliatelink.profile.dto.advertiserDto.AdvetiserResponseDTO;
import com.affiliatelink.profile.dto.advertiserDto.CreateAdvertiserRequestDto;
import com.affiliatelink.profile.entity.Advertiser;
import com.affiliatelink.profile.service.AdvertiserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/advertisers")
public class AdvertiserController {

    private AdvertiserService advertiserService;

    public AdvertiserController(AdvertiserService advertiserService) {
        this.advertiserService = advertiserService;
    }

    @PostMapping()
    public ResponseEntity<AdvetiserResponseDTO> createAdvetiser(@Valid @RequestBody CreateAdvertiserRequestDto createAdvertiserRequestDto) {
        AdvetiserResponseDTO advetiserResponseDTO = advertiserService.createAdvertiser(createAdvertiserRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(advetiserResponseDTO);
    }
}
