package com.affiliatelink.profile.entity;


import com.affiliatelink.profile.entity.Profile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_advertisers")
@PrimaryKeyJoinColumn(name = "id")
public class Advertiser extends Profile {

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "sector", nullable = false, length = 255)
    private String sector;

    @Column(name = "website", nullable = false, length = 255)
    private String website;

    protected Advertiser() {
    }

    public Advertiser(String email, String companyName, String sector, String website) {
        super(email);
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("companyName is required");
        }

        if (sector == null || sector.isBlank()) {
            throw new IllegalArgumentException("sector name is required");
        }

        if (website == null || website.isBlank()) {
            throw new IllegalArgumentException("website is required");
        }
        this.companyName = companyName;
        this.sector = sector;
        this.website = website;
    }

    public String getSector() {
        return sector;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getWebsite() {
        return website;
    }
}