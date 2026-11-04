package com.affiliatelink.offre.entity;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.offre.OffreNotValideException;
import jakarta.persistence.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.*;

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
        this.advertiserId = advertiserId;
        modifierInformations(
                title,
                description,
                landingUrl,
                niche
        );

    }


    public Long getId() {
        return id;
    }

    public Long getAdvertiserId() {
        return advertiserId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLandingUrl() {
        return landingUrl;
    }

    public Niche getNiche() {
        return niche;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void modifierInformations(
            String title,
            String description,
            String landingUrl,
            Niche niche
    ) {// 2. Vérifier le titre
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
        //    après avoir validé tous les paramètres
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
        this.niche = niche;
    }


    public void ajouterTag(Tag tag) {

        if (tag == null) {
            throw new IllegalArgumentException(
                    "Le tag est obligatoire"
            );
        }

        if (tags.add(tag)) {
            tag.associerOffre(this);
        }
    }

    public void retirerTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException(
                    "Le tag est obligatoire"
            );
        }
        if (tags.remove(tag)) {
            tag.dissocierOffre(this);
        }
    }

    public void remplacerTags(Set<Tag> nouveautags) {
        if (nouveautags == null || nouveautags.contains(null)) {
            throw new IllegalArgumentException(
                    "Les nouveaux tags sont invalides"
            );
        }

        for (Tag ancienTag : new HashSet<>(tags)) {
            if (!nouveautags.contains(ancienTag)) {
                retirerTag(ancienTag);
            }
        }

        for (Tag nouveauTag : nouveautags) {
            if (!tags.contains(nouveauTag)) {
                ajouterTag(nouveauTag);
            }
        }
    }
}
