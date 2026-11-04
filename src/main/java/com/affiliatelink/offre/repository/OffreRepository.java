package com.affiliatelink.offre.repository;

import com.affiliatelink.offre.entity.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OffreRepository extends JpaRepository<Offre, Long>, JpaSpecificationExecutor<Offre> {
}
