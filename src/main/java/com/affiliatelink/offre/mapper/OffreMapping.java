package com.affiliatelink.offre.mapper;

import com.affiliatelink.offre.dto.OffreResponse;
import com.affiliatelink.offre.dto.OffreDetailResponse;
import com.affiliatelink.offre.dto.CampagneResponse;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.entity.Tag;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface OffreMapping {

    @Mapping(
            target = "tags",
            source = "tags",
            qualifiedByName = "sortedTagNames"
    )
    OffreResponse toResponse(Offre offre);

    @Mapping(
            target = "tags",
            source = "offre.tags",
            qualifiedByName = "sortedTagNames"
    )
    @Mapping(
            target = "campagnes",
            source = "campagnes"
    )
    OffreDetailResponse toDetailResponse(
            Offre offre,
            List<CampagneResponse> campagnes
    );

    @Named("sortedTagNames")
    default List<String> sortedTagNames(Set<Tag> tags) {
        if (tags == null) {
            return List.of();
        }

        return tags.stream()
                .map(Tag::getName)
                .sorted()
                .toList();
    }
}