package com.affiliatelink.offre.entity;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.OffreNotValideException;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(
            name = "offre_offres_tags",
            joinColumns = @JoinColumn(name = "offre_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();


    protected Offre() {
    }

    public Offre(
            Long advertiserId,
            String title,
            String description,
            String landingUrl,
            Niche niche
    ) {

        // 1. Vérifier l'identifiant de l'annonceur
        if (advertiserId == null || advertiserId <= 0) {
            throw new OffreNotValideException(
                    "L'identifiant de l'annonceur doit être positif"
            );
        }

        // 2. Vérifier le titre
        if (title == null || title.isBlank()) {
            throw new OffreNotValideException(
                    "Le titre de l'offre est obligatoire"
            );
        }

        title = title.strip();

        if (title.length() > 255) {
            throw new OffreNotValideException(
                    "Le titre ne doit pas dépasser 255 caractères"
            );
        }

        // 3. Vérifier l'URL
        if (landingUrl == null || landingUrl.isBlank()) {
            throw new OffreNotValideException(
                    "L'URL de destination est obligatoire"
            );
        }

        landingUrl = landingUrl.strip();

        if (landingUrl.length() > 255) {
            throw new OffreNotValideException(
                    "L'URL ne doit pas dépasser 255 caractères"
            );
        }

        // 4. Vérifier la niche
        if (niche == null) {
            throw new OffreNotValideException(
                    "La niche est obligatoire"
            );
        }

        // 5. Initialiser l'entité
        this.advertiserId = advertiserId;
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
        this.niche = niche;
    }
}
