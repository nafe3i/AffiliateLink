package com.affiliatelink.offre.service;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.offre.StatusNotActiveException;
import com.affiliatelink.common.exception.profil.ProfileNotFoudException;
import com.affiliatelink.offre.dto.CreateOffreRequest;
import com.affiliatelink.offre.dto.OffreResponse;
import com.affiliatelink.offre.entity.Offre;
import com.affiliatelink.offre.entity.Tag;
import com.affiliatelink.offre.mapper.CampagneMapper;
import com.affiliatelink.offre.mapper.OffreMapping;
import com.affiliatelink.offre.repository.CampagneRepository;
import com.affiliatelink.offre.repository.OffreRepository;
import com.affiliatelink.offre.repository.TagRepository;
import com.affiliatelink.profile.ProfileApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffreServiceTest {
    @Mock private OffreRepository offreRepository;
    @Mock private TagRepository tagRepository;
    @Mock private ProfileApi profileApi;
    @Mock private CampagneMapper campagneMapper;
    @Mock private CampagneRepository campagneRepository;
    @Mock private OffreMapping offreMapping;
    @InjectMocks private OffreService offreService;

    private static CreateOffreRequest request(List<String> tags) {
        return new CreateOffreRequest(
                1L, "Offre Java", "Description", "https://example.com",
                Niche.TECH, tags);
    }

    @Test
    void shouldRefuseOfferWhenAdvertiserDoesNotExist() {
        CreateOffreRequest request = request(List.of("java"));
        doThrow(new ProfileNotFoudException("Advertiser not found"))
                .when(profileApi).requiredActiveAdvertiser(1L);

        assertThrows(ProfileNotFoudException.class, () -> offreService.creerOffre(request));
        verifyNoInteractions(offreRepository, tagRepository, offreMapping);
    }

    @Test
    void shouldRefuseOfferWhenAdvertiserIsNotActive() {
        CreateOffreRequest request = request(List.of("java"));
        doThrow(new StatusNotActiveException("Advertiser not active"))
                .when(profileApi).requiredActiveAdvertiser(1L);

        assertThrows(StatusNotActiveException.class, () -> offreService.creerOffre(request));
        verifyNoInteractions(offreRepository, tagRepository, offreMapping);
    }

    @Test
    void shouldNormalizeDeduplicateAndReuseOfferTags() {
        // Given : la 2e occurrence de 'java' récupère le tag déjà enregistré.
        CreateOffreRequest request = request(List.of(" JAVA ", "java", " SPRING "));
        Tag sharedJava = new Tag("java");
        when(tagRepository.findByName("java"))
                .thenReturn(Optional.empty(), Optional.of(sharedJava));
        when(tagRepository.findByName("spring")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));
        when(offreRepository.saveAndFlush(any(Offre.class))).thenAnswer(inv -> {
            Offre saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 9L);
            return saved;
        });
        OffreResponse expected = new OffreResponse(
                9L, 1L, "Offre Java", "Description", "https://example.com",
                Niche.TECH, null, List.of("java", "spring"));
        when(offreMapping.toResponse(any(Offre.class))).thenReturn(expected);

        TransactionSynchronizationManager.initSynchronization();
        try {
            // When
            OffreResponse result = offreService.creerOffre(request);

            // Then
            assertEquals(expected, result);
            ArgumentCaptor<Offre> captor = ArgumentCaptor.forClass(Offre.class);
            verify(offreRepository).saveAndFlush(captor.capture());
            Offre saved = captor.getValue();
            assertEquals(2, saved.getTags().size());
            assertEquals(Set.of("java", "spring"),
                    saved.getTags().stream().map(Tag::getName)
                            .collect(java.util.stream.Collectors.toSet()));
            assertTrue(saved.getTags().contains(sharedJava));
            verify(tagRepository, times(2)).save(any(Tag.class));
            verify(tagRepository, times(2)).findByName("java");
            verify(tagRepository).findByName("spring");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldReuseExistingTagInsteadOfCreatingAnother() {
        CreateOffreRequest request = request(List.of(" JAVA "));
        Tag existingTag = new Tag("java");
        when(tagRepository.findByName("java")).thenReturn(Optional.of(existingTag));
        when(offreRepository.saveAndFlush(any(Offre.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        OffreResponse expected = new OffreResponse(
                null, 1L, "Offre Java", "Description", "https://example.com",
                Niche.TECH, null, List.of("java"));
        when(offreMapping.toResponse(any(Offre.class))).thenReturn(expected);

        TransactionSynchronizationManager.initSynchronization();
        try {
            offreService.creerOffre(request);
            ArgumentCaptor<Offre> captor = ArgumentCaptor.forClass(Offre.class);
            verify(offreRepository).saveAndFlush(captor.capture());
            assertTrue(captor.getValue().getTags().contains(existingTag));
            verify(tagRepository, never()).save(any(Tag.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @ParameterizedTest(name = "champ de tri interdit = {0}")
    @MethodSource("invalidSortFields")
    void shouldRejectUnapprovedSortFields(String field) {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(field));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> offreService.getAllOffres(null, null, null, null, pageable));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(offreRepository);
    }

    static Stream<String> invalidSortFields() {
        return Stream.of("niche", "advertiserId", "unexpectedProperty");
    }
}
