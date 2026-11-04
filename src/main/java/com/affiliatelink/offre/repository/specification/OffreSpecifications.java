package com.affiliatelink.offre.repository.specification;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.offre.entity.Offre;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import com.affiliatelink.offre.entity.Tag;
import jakarta.persistence.criteria.Join;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OffreSpecifications {

    private OffreSpecifications() {
    }

    public static Specification<Offre> filtrer(
            Niche niche,
            Long advertiserId,
            String tagName,
            String keyword

    ) {
        return (root, query, cb) -> {

            List<Predicate> conditions = new ArrayList<>();

            if (niche != null) {
                conditions.add(cb.equal(root.get("niche"), niche));
            }

            if (advertiserId != null) {
                conditions.add(
                        cb.equal(
                                root.get("advertiserId"),
                                advertiserId
                        )
                );
            }
            if (keyword != null && !keyword.isBlank()) {

                String normalizedKeyword =
                        keyword.strip().toLowerCase(Locale.ROOT);

                conditions.add(
                        cb.like(
                                cb.lower(root.get("title")),
                                "%" + normalizedKeyword + "%"
                        )
                );
            }

            if (tagName != null && !tagName.isBlank()) {

                String normalizedTag =
                        tagName.strip().toLowerCase(Locale.ROOT);

                Join<Offre, Tag> tags = root.join("tags");

                conditions.add(
                        cb.equal(tags.get("name"), normalizedTag)
                );

                query.distinct(true);
            }


            return cb.and(
                    conditions.toArray(new Predicate[0])
            );
        };
    }
}