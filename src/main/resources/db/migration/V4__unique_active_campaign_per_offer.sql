CREATE UNIQUE INDEX uk_offre_campagnes_one_active
    ON offre_campagnes(offre_id)
    WHERE status = 'ACTIVE';