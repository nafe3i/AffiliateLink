package com.affiliatelink.offre.mapper;

import com.affiliatelink.offre.CampaignInfo;
import com.affiliatelink.offre.dto.CampagneResponse;
import com.affiliatelink.offre.entity.Campagne;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CampagneMapper {

    @Mapping(target = "offreId", source = "offre.id")
    CampagneResponse toResponse(Campagne campagne);

    List<CampagneResponse> toResponses(
            List<Campagne> campagnes
    );

    @Mapping(target = "offreId", source = "offre.id")
    CampaignInfo toCampaignInfo(Campagne campagne);
}