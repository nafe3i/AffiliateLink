
package com.affiliatelink.profile.mapper;

import com.affiliatelink.profile.dto.publisherDto.PublisherResponseDTO;
import com.affiliatelink.profile.entity.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PublisherMapper {

    PublisherResponseDTO toResponse(Publisher publisher);

    List<PublisherResponseDTO> toResponses(
            List<Publisher> publishers
    );
}
