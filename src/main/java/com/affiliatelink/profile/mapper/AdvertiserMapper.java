package com.affiliatelink.profile.mapper;

import com.affiliatelink.profile.dto.advertiserDto.AdvetiserResponseDTO;
import com.affiliatelink.profile.entity.Advertiser;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdvertiserMapper {

    @Mapping(target = "profileStatus", source = "status")
    AdvetiserResponseDTO toResponse(Advertiser advertiser);

    List<AdvetiserResponseDTO> toResponses(
            List<Advertiser> advertisers
    );
}