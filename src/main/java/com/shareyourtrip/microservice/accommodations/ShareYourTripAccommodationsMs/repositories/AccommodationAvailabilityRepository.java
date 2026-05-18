package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.repositories;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.AccommodationAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationAvailabilityRepository extends JpaRepository<AccommodationAvailability, Long> {
    List<AccommodationAvailability> findByAccommodationId(Long accommodationId);
}
