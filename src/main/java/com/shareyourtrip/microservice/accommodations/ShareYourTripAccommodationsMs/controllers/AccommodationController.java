package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.controllers;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationFilterRequestDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationRequestDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationResponseDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.services.AccommodationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accommodations")
public class AccommodationController {

    @Autowired
    private AccommodationService accommodationService;

    @GetMapping("/{id}")
    public AccommodationResponseDto getAccommodationById(@PathVariable Long id) {
        return accommodationService.getById(id);
    }

    @PostMapping("/search")
    public List<AccommodationResponseDto> search(@RequestBody AccommodationFilterRequestDto filter) {
        return accommodationService.filter(filter);
    }

    @PostMapping
    public AccommodationResponseDto create(@Valid @RequestBody AccommodationRequestDto request) {
        return accommodationService.create(request);
    }
}
