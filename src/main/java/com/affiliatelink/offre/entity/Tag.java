package com.affiliatelink.offre.entity;

import jakarta.persistence.*;

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

    protected Tag() {
    }
}