package com.affiliatelink.offre.controller;

import com.affiliatelink.offre.dto.CampagneResponse;
import com.affiliatelink.offre.dto.CreateCampagneRequest;
import com.affiliatelink.offre.dto.UpdateCampagneStatusRequest;
import com.affiliatelink.offre.entity.enums.CampagneStatus;
import com.affiliatelink.offre.service.CampagneService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CampagneController {

    private final CampagneService campagneService;

    public CampagneController(CampagneService campagneService) {
        this.campagneService = campagneService;
    }

    // Création d'une campagne pour une offre
    @PostMapping("/api/offres/{offreId}/campaigns")
    public ResponseEntity<CampagneResponse> createCampagne(
            @PathVariable Long offreId,
            @Valid @RequestBody CreateCampagneRequest request
    ) {
        CampagneResponse response =
                campagneService.createCampagne(offreId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Modification du statut d'une campagne
    @PatchMapping("/api/campaigns/{id}/status")
    public ResponseEntity<Void> changerStatut(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCampagneStatusRequest request
    ) {
        campagneService.changerStatut(
                id,
                request.status()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/campaigns")
    public ResponseEntity<Page<CampagneResponse>> listerCampagnes(
            @RequestParam(required = false) CampagneStatus status,
            @PageableDefault(
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                campagneService.listerCampagnes(status, pageable)
        );
    }
}