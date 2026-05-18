package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.services;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.*;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.*;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.exceptions.AccommodationNotFoundException;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.exceptions.Messages;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.exceptions.UserNotFoundException;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.integration.UserClient;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.mappers.AccommodationMapper;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.repositories.AccommodationAvailabilityRepository;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.repositories.AccommodationRepository;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.repositories.AccommodationSpecification;
import feign.FeignException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccommodationServiceImpl implements AccommodationService {

    @Autowired
    private AccommodationRepository accommodationRepository;
    @Autowired
    private AccommodationAvailabilityRepository accommodationAvailabilityRepository;
    @Autowired
    private UserClient userClient;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public AccommodationResponseDto getById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new AccommodationNotFoundException(Messages.ACCOMMODATION_NOT_FOUND_PREFIX + id));

        return AccommodationMapper.toDTO(accommodation);
    }

    @Override
    public List<AccommodationResponseDto> filter(AccommodationFilterRequestDto filter) {
        if (filter.getLanguage() != null && !filter.getLanguage().isBlank()) {
            List<Long> hostIds = userClient.getUserIdsByLanguage(filter.getLanguage());

            if (hostIds.isEmpty()) {
                return List.of();
            }

            filter.setHostIds(hostIds);
        }

        if ((filter.getCheckIn() != null && filter.getCheckOut() == null) ||
                (filter.getCheckIn() == null && filter.getCheckOut() != null)) {
            throw new IllegalArgumentException(Messages.CHECKIN_CHECKOUT_REQUIRED_TOGETHER);
        }

        if (filter.getCheckIn() != null && !filter.getCheckOut().isAfter(filter.getCheckIn())) {
            throw new IllegalArgumentException(Messages.CHECKOUT_AFTER_CHECKIN);
        }
        return accommodationRepository.findAll(AccommodationSpecification.withFilters(filter))
                .stream()
                .map(AccommodationMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public AccommodationResponseDto create(AccommodationRequestDto request) {
        UserResponseDto host;

        try {
            host = userClient.getUserById(request.getHostId());
        } catch (FeignException.NotFound ex) {
            throw new UserNotFoundException(Messages.USER_NOT_FOUND_PREFIX + request.getHostId());
        } catch (FeignException ex) {
            throw new IllegalStateException(Messages.ERROR_CALLING_USER_SERVICE);
        }

        if (!Boolean.TRUE.equals(host.getIsActive())) {
            throw new IllegalArgumentException(Messages.USER_NOT_ACTIVE_PREFIX + request.getHostId());
        }

        validateImages(request.getImages());
        Accommodation savedAccommodation = accommodationRepository.save(AccommodationMapper.toEntity(request));
        userClient.assignHostRole(request.getHostId());
        return AccommodationMapper.toDTO(savedAccommodation);
    }

    @Override
    public List<AccommodationResponseDto> findByHostId(Long hostId) {
        return accommodationRepository.findByHostId(hostId)
                .stream()
                .filter(a -> a.getStatus() != AccommodationStatus.DELETED)
                .map(AccommodationMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public AccommodationResponseDto update(Long id, AccommodationRequestDto request) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new AccommodationNotFoundException(Messages.ACCOMMODATION_NOT_FOUND_PREFIX + id));

        UserResponseDto host;
        try {
            host = userClient.getUserById(request.getHostId());
        } catch (FeignException.NotFound ex) {
            throw new UserNotFoundException(Messages.USER_NOT_FOUND_PREFIX + request.getHostId());
        } catch (FeignException ex) {
            throw new IllegalStateException(Messages.ERROR_CALLING_USER_SERVICE);
        }

        if (!Boolean.TRUE.equals(host.getIsActive())) {
            throw new IllegalArgumentException(Messages.USER_NOT_ACTIVE_PREFIX + request.getHostId());
        }

        validateImages(request.getImages());
        
        // Update fields
        accommodation.setHostId(request.getHostId());
        accommodation.setTitle(request.getTitle());
        accommodation.setDescription(request.getDescription());
        accommodation.setAddressLine(request.getAddressLine());
        accommodation.setCity(request.getCity());
        accommodation.setCountry(request.getCountry());
        accommodation.setPostalCode(request.getPostalCode());
        accommodation.setLatitude(request.getLatitude());
        accommodation.setLongitude(request.getLongitude());
        accommodation.setPricePerNight(request.getPricePerNight());
        accommodation.setMaxGuests(request.getMaxGuests());
        accommodation.setRoomType(request.getRoomType());
        accommodation.setRules(request.getRules());

        if (request.getStatus() != null) {
            try {
                accommodation.setStatus(AccommodationStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        
        // Update existing facility or create new one
        if (request.getFacilities() != null) {
            Facility facility = accommodation.getFacility();
            if (facility == null) {
                facility = Facility.builder()
                        .accommodation(accommodation)
                        .build();
            }
            facility.setWifi(request.getFacilities().getWifi());
            facility.setWashing(request.getFacilities().getWashing());
            facility.setAir(request.getFacilities().getAir());
            facility.setKitchen(request.getFacilities().getKitchen());
            accommodation.setFacility(facility);
        }
        
        accommodation.setUpdatedAt(LocalDateTime.now());

        // Handle images
        if (request.getImages() != null) {
            accommodation.getImages().clear();
            entityManager.flush();
            List<AccommodationImage> newImages = request.getImages().stream()
                    .map(img -> AccommodationMapper.imageRequestToEntity(img, accommodation))
                    .collect(java.util.stream.Collectors.toList());
            accommodation.getImages().addAll(newImages);
        }

        // Handle availabilities
        if (request.getAvailabilities() != null) {
            accommodation.getAvailabilities().clear();
            entityManager.flush();
            List<AccommodationAvailability> newAvailabilities = request.getAvailabilities().stream()
                    .map(dto -> AccommodationMapper.availabilityDtoToEntity(dto, accommodation))
                    .collect(java.util.stream.Collectors.toList());
            accommodation.getAvailabilities().addAll(newAvailabilities);
        }

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        return AccommodationMapper.toDTO(savedAccommodation);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new AccommodationNotFoundException(Messages.ACCOMMODATION_NOT_FOUND_PREFIX + id));
        
        Long hostId = accommodation.getHostId();
        List<AccommodationResponseDto> hostAccommodations = findByHostId(hostId);
        
        accommodation.setStatus(AccommodationStatus.DELETED);
        accommodation.setUpdatedAt(LocalDateTime.now());
        accommodationRepository.save(accommodation);
        
        if (hostAccommodations.size() == 1) {
            userClient.assignTravelerRole(hostId);
        }
    }

    @Override
    public List<AvailabilityDto> getAvailabilityDates(Long accommodationId) {
        try {
            List<AccommodationAvailability> availabilities = accommodationAvailabilityRepository.findByAccommodationId(accommodationId);
            if (availabilities == null || availabilities.isEmpty()) {
                return List.of();
            }
            return availabilities.stream()
                    .map(availability -> AvailabilityDto.builder()
                            .availableDate(availability.getAvailableDate())
                            .isAvailable(availability.getIsAvailable())
                            .build())
                    .toList();
        } catch (Exception e) {
            System.err.println("Error al obtener disponibilidades: " + e.getMessage());
            return List.of();
        }
    }

    private void validateImages(List<AccommodationImageRequestDTO> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        long coverCount = images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsCover()))
                .count();

        if (coverCount > 1) {
            throw new IllegalArgumentException(Messages.ONLY_ONE_COVER_IMAGE);
        }
    }
}
