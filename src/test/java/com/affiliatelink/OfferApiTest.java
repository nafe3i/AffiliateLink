package com.affiliatelink;

import com.affiliatelink.offre.CampaignInfo;
import com.affiliatelink.offre.OfferApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OfferApiTest {

    @Autowired
    private OfferApi offerApi;

    @Test
    void shouldGetCampaign() {

        CampaignInfo campaign = offerApi.getCampaign(2L);

        assertNotNull(campaign);
        assertEquals(2L, campaign.id());
        assertEquals(5L, campaign.offreId());
        assertEquals("PAUSED", campaign.status());
    }
}