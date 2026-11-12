package com.affiliatelink.profile.service;

import com.affiliatelink.common.domain.Niche;
import com.affiliatelink.common.exception.profil.EmailAlreadyExistsException;
import com.affiliatelink.common.exception.profil.ProfileNotFoudException;
import com.affiliatelink.profile.dto.publisherDto.CreatePublisherRequestDTO;
import com.affiliatelink.profile.dto.publisherDto.PublisherResponseDTO;
import com.affiliatelink.profile.entity.Publisher;
import com.affiliatelink.profile.entity.enums.ProfileStatus;
import com.affiliatelink.profile.mapper.PublisherMapper;
import com.affiliatelink.profile.repository.ProfileRepository;
import com.affiliatelink.profile.repository.PublisherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {
    @Mock
    private PublisherRepository publisherRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PublisherMapper publisherMapper;
    @InjectMocks
    private PublisherService publisherService;

    private static CreatePublisherRequestDTO request() {
        return new CreatePublisherRequestDTO(
                "publisher@example.com", "Ali", "Test", Niche.TECH,
                "Java", "Morocco");
    }

    @Test
    void shouldRejectDuplicatePublisherEmailWithoutSaving() {
        CreatePublisherRequestDTO request = request();
        when(profileRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> publisherService.createpublisher(request));
        verifyNoInteractions(publisherRepository, publisherMapper);
    }

    @Test
    void shouldCreatePendingPublisherWhenEmailIsAvailable() {
        CreatePublisherRequestDTO request = request();
        Publisher saved = new Publisher(request.email(), request.firstName(),
                request.lastName(), request.niche(), request.bio(), request.country());
        ReflectionTestUtils.setField(saved, "id", 4L);
        PublisherResponseDTO expected = new PublisherResponseDTO(
                4L, request.email(), request.firstName(), request.lastName(),
                request.niche(), request.bio(), request.country(),
                ProfileStatus.PENDING, null);

        when(publisherRepository.save(any(Publisher.class))).thenReturn(saved);
        when(publisherMapper.toResponse(saved)).thenReturn(expected);
        TransactionSynchronizationManager.initSynchronization();
        try {
            PublisherResponseDTO result = publisherService.createpublisher(request);
            assertEquals(expected, result);
            ArgumentCaptor<Publisher> captor = ArgumentCaptor.forClass(Publisher.class);
            verify(publisherRepository).save(captor.capture());
            Publisher created = captor.getValue();
            assertAll(
                    () -> assertEquals(request.email(), created.getEmail()),
                    () -> assertEquals(request.firstName(), created.getFirstName()),
                    () -> assertEquals(request.niche(), created.getNiche()),
                    () -> assertEquals(ProfileStatus.PENDING, created.getStatus())
            );
            verify(publisherMapper).toResponse(saved);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldThrowWhenPublisherNotFound() {
        when(publisherRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ProfileNotFoudException.class, () -> publisherService.getPublisher(99L));
        verifyNoInteractions(publisherMapper);
    }

    @ParameterizedTest(name = "id invalide : {0}")
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void shouldRejectInvalidPublisherId(Long id) {
        assertThrows(ProfileNotFoudException.class, () -> publisherService.getPublisher(id));
        verifyNoInteractions(publisherRepository, publisherMapper);
    }
}
