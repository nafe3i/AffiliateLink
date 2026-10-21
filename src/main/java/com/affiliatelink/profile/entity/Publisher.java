package com.affiliatelink.profile.entity;

import com.affiliatelink.profile.entity.enums.Niche;
import jakarta.persistence.*;

@Entity
@Table(name = "profile_publishers")
@PrimaryKeyJoinColumn(name = "id")
public class Publisher extends Profile {

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "niche", nullable = false, length = 50)
    private Niche niche;

    @Column(name = "bio", length = 50)
    private String bio;

    @Column(name = "country", length = 50)
    private String country;

    protected Publisher() {
    }

    public Publisher(
            String email,
            String firstName,
            String lastName,
            Niche niche,
            String bio,
            String country
    ) {
        super(email);

        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (niche == null) {
            throw new IllegalArgumentException("Niche is required");
        }

        this.firstName = firstName;
        this.lastName = lastName;
        this.niche = niche;
        this.bio = bio;
        this.country = country;
    }

    public String getBio() {
        return bio;
    }

    public Niche getNiche() {
        return niche;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getCountry() {
        return country;
    }
}
