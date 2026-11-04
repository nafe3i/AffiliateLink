package com.affiliatelink.offre.repository.specification;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.offre.entity.Offre;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class OffreSpecifications {

    private OffreSpecifications() {
    }

    public static Specification<Offre> filtrer(
            Niche niche,
            Long advertiserId
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

            return cb.and(
                    conditions.toArray(new Predicate[0])
            );
        };
    }
}