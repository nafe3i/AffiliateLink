package com.affiliatelink.profile.controller;


import com.affiliatelink.profile.dto.advertiserDto.AdvetiserResponseDTO;
import com.affiliatelink.profile.dto.advertiserDto.CreateAdvertiserRequestDto;
import com.affiliatelink.profile.service.AdvertiserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping()
    public ResponseEntity<List<AdvetiserResponseDTO>> getAllAdvertisers() {
        List<AdvetiserResponseDTO> advertiser = advertiserService.getAllAdvertisers();
        return ResponseEntity.ok(advertiser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvetiserResponseDTO> getAdveAdvetiser(@PathVariable("id") Long id) {
        AdvetiserResponseDTO advetiserResponseDTO = advertiserService.getAdvertiser(id);
        return ResponseEntity.ok(advetiserResponseDTO);
    }
}
