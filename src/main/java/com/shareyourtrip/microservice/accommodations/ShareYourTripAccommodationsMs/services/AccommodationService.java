package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.services;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationFilterRequestDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationRequestDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationResponseDto;

import java.util.List;

public interface AccommodationService {
    AccommodationResponseDto getById(Long id);
    List<AccommodationResponseDto> filter(AccommodationFilterRequestDto filter);
    AccommodationResponseDto create(AccommodationRequestDto request);
}
