package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.controllers;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationFilterRequestDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationRequestDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationResponseDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AvailabilityDto;
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

    @GetMapping("/host/{hostId}")
    public List<AccommodationResponseDto> findByHostId(@PathVariable Long hostId) {
        return accommodationService.findByHostId(hostId);
    }

    @PutMapping("/{id}")
    public AccommodationResponseDto update(@PathVariable Long id, @Valid @RequestBody AccommodationRequestDto request) {
        return accommodationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        accommodationService.delete(id);
    }

    @GetMapping("/{id}/availability")
    public List<AvailabilityDto> getAvailabilityDates(@PathVariable Long id) {
        return accommodationService.getAvailabilityDates(id);
    }
}
