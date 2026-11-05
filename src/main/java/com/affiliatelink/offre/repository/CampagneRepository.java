package com.affiliatelink.offre.repository;

import com.affiliatelink.offre.entity.Campagne;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.entity.enums.CampagneStatus;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CampagneRepository
        extends JpaRepository<Campagne, Long> {

    // 1. Lister les campagnes par statut
    Page<Campagne> findByStatus(
            CampagneStatus status,
            Pageable pageable
    );

    // 2. Trouver les autres campagnes actives d'une offre
    List<Campagne> findByOffre_IdAndStatusAndIdNot(
            Long offreId,
            CampagneStatus status,
            Long excludedId
    );

    // 3. Récupérer l'identifiant de l'offre d'une campagne
    @Query("""
            SELECT c.offre.id
            FROM Campagne c
            WHERE c.id = :campagneId
            """)
    Optional<Long> findOffreIdByCampagneId(
            @Param("campagneId") Long campagneId
    );

    // 4. Trouver les campagnes d'un annonceur par statut
    List<Campagne> findByOffre_AdvertiserIdAndStatus(
            Long advertiserId,
            CampagneStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Offre o WHERE o.id = :id")
    Optional<Offre> findByIdForUpdate(@Param("id") Long id);

    List<Campagne> findByOffre_IdOrderByIdDesc(Long offreId);

}