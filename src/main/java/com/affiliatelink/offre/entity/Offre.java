package com.affiliatelink.offre.entity;

import com.affiliatelink.common.domain.Niche;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity()
@Table(name = "offre_offres")
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "advertiser_id", nullable = false)
    private Long advertiserId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "landing_url", nullable = false)
    private String landingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Niche niche;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @OneToMany(mappedBy = "offre")
    private List<Campagne> campagnes = new ArrayList<>();

    protected Offre() {
    }
}
