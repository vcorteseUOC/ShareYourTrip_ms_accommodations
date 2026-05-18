package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.services;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationFilterRequestDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationRequestDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationResponseDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AvailabilityDto;

import java.util.List;

public interface AccommodationService {
    AccommodationResponseDto getById(Long id);
    List<AccommodationResponseDto> filter(AccommodationFilterRequestDto filter);
    AccommodationResponseDto create(AccommodationRequestDto request);
    List<AccommodationResponseDto> findByHostId(Long hostId);
    AccommodationResponseDto update(Long id, AccommodationRequestDto request);
    void delete(Long id);
    List<AvailabilityDto> getAvailabilityDates(Long accommodationId);
}
