package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.services;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.*;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.*;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.exceptions.AccommodationNotFoundException;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.exceptions.UserNotFoundException;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.integration.UserClient;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.mappers.AccommodationMapper;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.repositories.AccommodationRepository;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.repositories.AccommodationSpecification;
import feign.FeignException;
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
    private UserClient userClient;

    @Override
    public AccommodationResponseDto getById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found with id: " + id));

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
            throw new IllegalArgumentException("checkIn and checkOut must be provided together");
        }

        if (filter.getCheckIn() != null && !filter.getCheckOut().isAfter(filter.getCheckIn())) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
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
            throw new UserNotFoundException("User not found with id: " + request.getHostId());
        } catch (FeignException ex) {
            throw new IllegalStateException("Error calling user service");
        }

        if (!Boolean.TRUE.equals(host.getIsActive())) {
            throw new IllegalArgumentException("User is not active: " + request.getHostId());
        }

        validateImages(request.getImages());
        Accommodation savedAccommodation = accommodationRepository.save(AccommodationMapper.toEntity(request));
        return AccommodationMapper.toDTO(savedAccommodation);
    }

    private void validateImages(List<AccommodationImageRequestDTO> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        long coverCount = images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsCover()))
                .count();

        if (coverCount > 1) {
            throw new IllegalArgumentException("Only one image can be marked as cover");
        }
    }
}
