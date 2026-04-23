package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.repositories;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long>, JpaSpecificationExecutor<Accommodation> {
}
