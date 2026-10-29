package com.affiliatelink.offre.entity;

import com.affiliatelink.common.exception.TagInvalideException;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "offre_tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true
    )
    private String name;


    @ManyToMany(mappedBy = "tags")
    private Set<Offre> offres = new HashSet<>();

    protected Tag() {
    }

    public Tag(String name) {

        if (name == null || name.isBlank()) {
            throw new TagInvalideException(
                    "Le nom du tag est obligatoire"
            );
        }

        String normalizedName =
                name.strip().toLowerCase(Locale.ROOT);

        if (normalizedName.length() > 255) {
            throw new TagInvalideException(
                    "Le nom du tag ne doit pas dépasser 255 caractères"
            );
        }

        this.name = normalizedName;
    }
}