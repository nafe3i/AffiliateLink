
package com.affiliatelink.offre.controller;

import com.affiliatelink.offre.dto.CreateOffreRequest;
import com.affiliatelink.offre.dto.OffreResponse;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.service.OffreService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offres")
public class OffreController {

    private final OffreService offreService;

    public OffreController(OffreService offreService) {
        this.offreService = offreService;
    }

    @PostMapping
    public ResponseEntity<OffreResponse> creerOffre(
            @Valid @RequestBody CreateOffreRequest request
    ) {

        OffreResponse response = offreService.creerOffre(request);

        URI location = URI.create(
                "/api/offres/" + response.id()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<OffreResponse>> getAllOffres() {
        return ResponseEntity.ok(offreService.getAllOffres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffreResponse> getOffre(@PathVariable("id") Long offreId) {
        return ResponseEntity.ok(offreService.getOffre(offreId));
    }

}
