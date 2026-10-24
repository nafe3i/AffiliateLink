package com.affiliatelink.profile.controller;


import com.affiliatelink.profile.dto.ProfileDTO;
import com.affiliatelink.profile.entity.enums.ProfileStatus;
import com.affiliatelink.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/profiles/{id}/status")
public class AdminController {
    private ProfileService profileService;

    public AdminController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping()
    public ResponseEntity<ProfileDTO> changerStatus(@PathVariable("id") Long idProfile) {
        return ResponseEntity.ok(profileService.changeProfileStatus(idProfile));
    }
}
