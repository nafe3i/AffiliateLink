
package com.affiliatelink.offre.controller;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.offre.dto.CreateOffreRequest;
import com.affiliatelink.offre.dto.OffreResponse;
import com.affiliatelink.offre.dto.UpdateOffreRequest;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.service.OffreService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<Page<OffreResponse>> getAllOffres(@RequestParam(name = "niche", required = false)
                                                            Niche niche,
                                                            @RequestParam(name = "advertiserId", required = false)
                                                            Long advertiserId,
                                                            @RequestParam(name = "tag", required = false)
                                                            String tagName,
                                                            @RequestParam(value = "keyword", required = false)
                                                            String keyword,
                                                            @PageableDefault(
                                                                    page = 0,
                                                                    size = 10,
                                                                    sort = "createdAt",
                                                                    direction = Sort.Direction.DESC
                                                            )
                                                            Pageable pageable
//                                                            @RequestParam(defaultValue = "0") Integer page,
//                                                            @RequestParam(defaultValue = "10") Integer size,
//                                                            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return ResponseEntity.ok(
                offreService.getAllOffres(
                        niche,
                        advertiserId,
                        tagName,
                        keyword,
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffreResponse> getOffre(@PathVariable("id") Long offreId) {
        return ResponseEntity.ok(offreService.getOffre(offreId));
    }


    @PutMapping("/{id}")
    public ResponseEntity<OffreResponse> modifierOffre(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOffreRequest request
    ) {
        return ResponseEntity.ok(
                offreService.modifierOffre(id, request)
        );
    }

}
