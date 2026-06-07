package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.services;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.*;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.*;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.exceptions.AccommodationNotFoundException;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.exceptions.UserNotFoundException;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.integration.UserClient;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.repositories.AccommodationAvailabilityRepository;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.repositories.AccommodationRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccommodationServiceImpl - Tests unitarios")
class AccommodationServiceImplTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationAvailabilityRepository accommodationAvailabilityRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    private Accommodation sampleAccommodation;
    private UserResponseDto activeHost;

    @BeforeEach
    void setUp() {
        activeHost = UserResponseDto.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("García")
                .email("juan@example.com")
                .isActive(true)
                .language("es")
                .build();

        sampleAccommodation = Accommodation.builder()
                .id(1L)
                .hostId(1L)
                .title("Apartamento en Barcelona")
                .description("Hermoso apartamento en el centro")
                .addressLine("Calle Mayor 10")
                .city("Barcelona")
                .country("España")
                .postalCode("08001")
                .latitude(new BigDecimal("41.385639"))
                .longitude(new BigDecimal("2.170068"))
                .pricePerNight(new BigDecimal("85.00"))
                .maxGuests(4)
                .roomType("APARTMENT")
                .status(AccommodationStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .images(new ArrayList<>())
                .availabilities(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("Debe retornar DTO cuando el alojamiento existe")
        void shouldReturnDtoWhenAccommodationExists() {
            when(accommodationRepository.findById(1L)).thenReturn(Optional.of(sampleAccommodation));

            AccommodationResponseDto result = accommodationService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Apartamento en Barcelona");
            assertThat(result.getCity()).isEqualTo("Barcelona");
            assertThat(result.getStatus()).isEqualTo(AccommodationStatus.ACTIVE);
        }

        @Test
        @DisplayName("Debe lanzar AccommodationNotFoundException cuando no existe")
        void shouldThrowWhenNotFound() {
            when(accommodationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accommodationService.getById(99L))
                    .isInstanceOf(AccommodationNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        @DisplayName("Debe filtrar por idioma cuando se proporciona")
        void shouldFilterByLanguage() {
            AccommodationFilterRequestDto filter = AccommodationFilterRequestDto.builder()
                    .language("es")
                    .build();

            when(userClient.getUserIdsByLanguage("es")).thenReturn(List.of(1L, 2L));
            when(accommodationRepository.findAll(any(Specification.class)))
                    .thenReturn(List.of(sampleAccommodation));

            List<AccommodationResponseDto> result = accommodationService.filter(filter);

            assertThat(result).hasSize(1);
            assertThat(filter.getHostIds()).containsExactly(1L, 2L);
            verify(accommodationRepository).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay hosts con ese idioma")
        void shouldReturnEmptyWhenNoHostsForLanguage() {
            AccommodationFilterRequestDto filter = AccommodationFilterRequestDto.builder()
                    .language("zh")
                    .build();

            when(userClient.getUserIdsByLanguage("zh")).thenReturn(Collections.emptyList());

            List<AccommodationResponseDto> result = accommodationService.filter(filter);

            assertThat(result).isEmpty();
            verify(accommodationRepository, never()).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando solo checkIn está presente")
        void shouldThrowWhenOnlyCheckInProvided() {
            AccommodationFilterRequestDto filter = AccommodationFilterRequestDto.builder()
                    .checkIn(LocalDate.of(2026, 7, 1))
                    .build();

            assertThatThrownBy(() -> accommodationService.filter(filter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("checkIn and checkOut must be provided together");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando solo checkOut está presente")
        void shouldThrowWhenOnlyCheckOutProvided() {
            AccommodationFilterRequestDto filter = AccommodationFilterRequestDto.builder()
                    .checkOut(LocalDate.of(2026, 7, 10))
                    .build();

            assertThatThrownBy(() -> accommodationService.filter(filter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("checkIn and checkOut must be provided together");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando checkOut no es posterior a checkIn")
        void shouldThrowWhenCheckOutNotAfterCheckIn() {
            AccommodationFilterRequestDto filter = AccommodationFilterRequestDto.builder()
                    .checkIn(LocalDate.of(2026, 7, 10))
                    .checkOut(LocalDate.of(2026, 7, 5))
                    .build();

            assertThatThrownBy(() -> accommodationService.filter(filter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("checkOut must be after checkIn");
        }

        @Test
        @DisplayName("Debe filtrar sin idioma correctamente")
        void shouldFilterWithoutLanguage() {
            AccommodationFilterRequestDto filter = AccommodationFilterRequestDto.builder()
                    .city("Barcelona")
                    .build();

            when(accommodationRepository.findAll(any(Specification.class)))
                    .thenReturn(List.of(sampleAccommodation));

            List<AccommodationResponseDto> result = accommodationService.filter(filter);

            assertThat(result).hasSize(1);
            verify(userClient, never()).getUserIdsByLanguage(anyString());
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        private AccommodationRequestDto request;

        @BeforeEach
        void setUpRequest() {
            request = AccommodationRequestDto.builder()
                    .hostId(1L)
                    .title("Nuevo apartamento")
                    .description("Descripción del apartamento")
                    .addressLine("Calle Nueva 5")
                    .city("Madrid")
                    .country("España")
                    .pricePerNight(new BigDecimal("100.00"))
                    .maxGuests(3)
                    .roomType("APARTMENT")
                    .build();
        }

        @Test
        @DisplayName("Debe crear alojamiento y asignar rol HOST")
        void shouldCreateAndAssignHostRole() {
            when(userClient.getUserById(1L)).thenReturn(activeHost);
            when(accommodationRepository.save(any(Accommodation.class))).thenAnswer(invocation -> {
                Accommodation a = invocation.getArgument(0);
                a.setId(10L);
                return a;
            });

            AccommodationResponseDto result = accommodationService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.getHostId()).isEqualTo(1L);
            verify(userClient).assignHostRole(1L);
            verify(accommodationRepository).save(any(Accommodation.class));
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundException cuando el host no existe (Feign 404)")
        void shouldThrowWhenHostNotFound() {
            FeignException.NotFound notFound = new FeignException.NotFound(
                    "Not Found",
                    Request.create(Request.HttpMethod.GET, "/users/99", java.util.Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate()),
                    null, null
            );
            when(userClient.getUserById(99L)).thenThrow(notFound);

            request.setHostId(99L);

            assertThatThrownBy(() -> accommodationService.create(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");

            verify(accommodationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException cuando falla el servicio de usuarios")
        void shouldThrowWhenUserServiceFails() {
            FeignException feignError = new FeignException.InternalServerError(
                    "Internal Server Error",
                    Request.create(Request.HttpMethod.GET, "/users/1", java.util.Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate()),
                    null, null
            );
            when(userClient.getUserById(1L)).thenThrow(feignError);

            assertThatThrownBy(() -> accommodationService.create(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Error calling user service");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el host está inactivo")
        void shouldThrowWhenHostInactive() {
            activeHost.setIsActive(false);
            when(userClient.getUserById(1L)).thenReturn(activeHost);

            assertThatThrownBy(() -> accommodationService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando hay más de una imagen de portada")
        void shouldThrowWhenMultipleCoverImages() {
            request.setImages(List.of(
                    AccommodationImageRequestDTO.builder().imageUrl("img1.jpg").isCover(true).build(),
                    AccommodationImageRequestDTO.builder().imageUrl("img2.jpg").isCover(true).build()
            ));
            when(userClient.getUserById(1L)).thenReturn(activeHost);

            assertThatThrownBy(() -> accommodationService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Only one image can be marked as cover");
        }

        @Test
        @DisplayName("Debe permitir crear con una sola imagen de portada")
        void shouldAllowOneCoverImage() {
            request.setImages(List.of(
                    AccommodationImageRequestDTO.builder().imageUrl("img1.jpg").isCover(true).build(),
                    AccommodationImageRequestDTO.builder().imageUrl("img2.jpg").isCover(false).build()
            ));
            when(userClient.getUserById(1L)).thenReturn(activeHost);
            when(accommodationRepository.save(any(Accommodation.class))).thenAnswer(invocation -> {
                Accommodation a = invocation.getArgument(0);
                a.setId(10L);
                return a;
            });

            AccommodationResponseDto result = accommodationService.create(request);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("findByHostId")
    class FindByHostId {

        @Test
        @DisplayName("Debe retornar alojamientos del host filtrando DELETED")
        void shouldReturnNonDeletedAccommodations() {
            Accommodation deletedAccommodation = Accommodation.builder()
                    .id(2L).hostId(1L).title("Borrado")
                    .status(AccommodationStatus.DELETED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .images(new ArrayList<>())
                    .availabilities(new ArrayList<>())
                    .build();

            when(accommodationRepository.findByHostId(1L))
                    .thenReturn(List.of(sampleAccommodation, deletedAccommodation));

            List<AccommodationResponseDto> result = accommodationService.findByHostId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isNotEqualTo(AccommodationStatus.DELETED);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si el host no tiene alojamientos")
        void shouldReturnEmptyList() {
            when(accommodationRepository.findByHostId(99L)).thenReturn(Collections.emptyList());

            List<AccommodationResponseDto> result = accommodationService.findByHostId(99L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        private AccommodationRequestDto request;

        @BeforeEach
        void setUpRequest() {
            request = AccommodationRequestDto.builder()
                    .hostId(1L)
                    .title("Título actualizado")
                    .description("Descripción actualizada")
                    .addressLine("Calle Actualizada 20")
                    .city("Barcelona")
                    .country("España")
                    .pricePerNight(new BigDecimal("120.00"))
                    .maxGuests(5)
                    .roomType("APARTMENT")
                    .build();
        }

        @Test
        @DisplayName("Debe actualizar campos del alojamiento")
        void shouldUpdateAccommodationFields() {
            when(accommodationRepository.findById(1L)).thenReturn(Optional.of(sampleAccommodation));
            when(userClient.getUserById(1L)).thenReturn(activeHost);
            when(accommodationRepository.save(any(Accommodation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            AccommodationResponseDto result = accommodationService.update(1L, request);

            assertThat(result).isNotNull();
            assertThat(sampleAccommodation.getTitle()).isEqualTo("Título actualizado");
            assertThat(sampleAccommodation.getPricePerNight()).isEqualByComparingTo(new BigDecimal("120.00"));
            assertThat(sampleAccommodation.getMaxGuests()).isEqualTo(5);
            verify(accommodationRepository).save(sampleAccommodation);
        }

        @Test
        @DisplayName("Debe lanzar AccommodationNotFoundException si no existe")
        void shouldThrowWhenAccommodationNotFound() {
            when(accommodationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accommodationService.update(99L, request))
                    .isInstanceOf(AccommodationNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundException si el host no existe (Feign 404)")
        void shouldThrowWhenHostNotFound() {
            when(accommodationRepository.findById(1L)).thenReturn(Optional.of(sampleAccommodation));
            FeignException.NotFound notFound = new FeignException.NotFound(
                    "Not Found",
                    Request.create(Request.HttpMethod.GET, "/users/99", java.util.Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate()),
                    null, null
            );
            when(userClient.getUserById(99L)).thenThrow(notFound);

            request.setHostId(99L);

            assertThatThrownBy(() -> accommodationService.update(1L, request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("Debe actualizar facilities correctamente")
        void shouldUpdateFacilities() {
            request.setFacilities(FacilityDTO.builder().wifi(true).washing(false).air(true).kitchen(true).build());
            when(accommodationRepository.findById(1L)).thenReturn(Optional.of(sampleAccommodation));
            when(userClient.getUserById(1L)).thenReturn(activeHost);
            when(accommodationRepository.save(any(Accommodation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            accommodationService.update(1L, request);

            assertThat(sampleAccommodation.getFacility()).isNotNull();
            assertThat(sampleAccommodation.getFacility().getWifi()).isTrue();
            assertThat(sampleAccommodation.getFacility().getKitchen()).isTrue();
        }

        @Test
        @DisplayName("Debe actualizar status cuando se proporciona")
        void shouldUpdateStatus() {
            request.setStatus("INACTIVE");
            when(accommodationRepository.findById(1L)).thenReturn(Optional.of(sampleAccommodation));
            when(userClient.getUserById(1L)).thenReturn(activeHost);
            when(accommodationRepository.save(any(Accommodation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            accommodationService.update(1L, request);

            assertThat(sampleAccommodation.getStatus()).isEqualTo(AccommodationStatus.INACTIVE);
        }

        @Test
        @DisplayName("No debe cambiar status si el valor es inválido")
        void shouldNotChangeStatusForInvalidValue() {
            request.setStatus("INVALID_STATUS");
            when(accommodationRepository.findById(1L)).thenReturn(Optional.of(sampleAccommodation));
            when(userClient.getUserById(1L)).thenReturn(activeHost);
            when(accommodationRepository.save(any(Accommodation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            accommodationService.update(1L, request);

            assertThat(sampleAccommodation.getStatus()).isEqualTo(AccommodationStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Debe marcar como DELETED y asignar TRAVELER si era el único alojamiento del host")
        void shouldDeleteAndAssignTravelerIfOnlyAccommodation() {
            when(accommodationRepository.findById(1L)).thenReturn(Optional.of(sampleAccommodation));
            when(accommodationRepository.findByHostId(1L)).thenReturn(List.of(sampleAccommodation));
            when(accommodationRepository.save(any(Accommodation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            accommodationService.delete(1L);

            assertThat(sampleAccommodation.getStatus()).isEqualTo(AccommodationStatus.DELETED);
            verify(userClient).assignTravelerRole(1L);
            verify(accommodationRepository).save(sampleAccommodation);
        }

        @Test
        @DisplayName("Debe marcar como DELETED sin asignar TRAVELER si hay otros alojamientos")
        void shouldDeleteWithoutAssigningTravelerIfMultipleAccommodations() {
            Accommodation otherAccommodation = Accommodation.builder()
                    .id(2L).hostId(1L).title("Otro")
                    .status(AccommodationStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .images(new ArrayList<>())
                    .availabilities(new ArrayList<>())
                    .build();

            when(accommodationRepository.findById(1L)).thenReturn(Optional.of(sampleAccommodation));
            when(accommodationRepository.findByHostId(1L))
                    .thenReturn(List.of(sampleAccommodation, otherAccommodation));
            when(accommodationRepository.save(any(Accommodation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            accommodationService.delete(1L);

            assertThat(sampleAccommodation.getStatus()).isEqualTo(AccommodationStatus.DELETED);
            verify(userClient, never()).assignTravelerRole(anyLong());
        }

        @Test
        @DisplayName("Debe lanzar AccommodationNotFoundException si no existe")
        void shouldThrowWhenNotFound() {
            when(accommodationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accommodationService.delete(99L))
                    .isInstanceOf(AccommodationNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAvailabilityDates")
    class GetAvailabilityDates {

        @Test
        @DisplayName("Debe retornar lista de AvailabilityDto")
        void shouldReturnAvailabilityDtos() {
            AccommodationAvailability availability = AccommodationAvailability.builder()
                    .id(1L)
                    .accommodation(sampleAccommodation)
                    .availableDate(LocalDate.of(2026, 7, 1))
                    .isAvailable(true)
                    .build();

            when(accommodationAvailabilityRepository.findByAccommodationId(1L))
                    .thenReturn(List.of(availability));

            List<AvailabilityDto> result = accommodationService.getAvailabilityDates(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAvailableDate()).isEqualTo(LocalDate.of(2026, 7, 1));
            assertThat(result.get(0).getIsAvailable()).isTrue();
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay disponibilidades")
        void shouldReturnEmptyListWhenNoAvailabilities() {
            when(accommodationAvailabilityRepository.findByAccommodationId(1L))
                    .thenReturn(Collections.emptyList());

            List<AvailabilityDto> result = accommodationService.getAvailabilityDates(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar lista vacía en caso de excepción")
        void shouldReturnEmptyListOnException() {
            when(accommodationAvailabilityRepository.findByAccommodationId(1L))
                    .thenThrow(new RuntimeException("DB error"));

            List<AvailabilityDto> result = accommodationService.getAvailabilityDates(1L);

            assertThat(result).isEmpty();
        }
    }
}
