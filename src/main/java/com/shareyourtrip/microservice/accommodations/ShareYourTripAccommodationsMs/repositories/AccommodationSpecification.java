package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.repositories;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationFilterRequestDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.Accommodation;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.AccommodationStatus;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.Facility;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class AccommodationSpecification {

    public static Specification<Accommodation> withFilters(AccommodationFilterRequestDto filter) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Para evitar duplicados en joins
            query.distinct(true);

            // Solo alojamientos activos
            predicates.add(cb.equal(root.get("status"), AccommodationStatus.ACTIVE));

            if (filter.getCity() != null && !filter.getCity().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("city")),
                                "%" + filter.getCity().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getCountry() != null && !filter.getCountry().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("country")),
                                "%" + filter.getCountry().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getHostId() != null) {
                predicates.add(cb.equal(root.get("hostId"), filter.getHostId()));
            }

            if (filter.getHostIds() != null && !filter.getHostIds().isEmpty()) {
                predicates.add(root.get("hostId").in(filter.getHostIds()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerNight"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerNight"), filter.getMaxPrice()));
            }

            if (filter.getGuests() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxGuests"), filter.getGuests()));
            }

            if (filter.getRoomType() != null && !filter.getRoomType().isBlank()) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("roomType")),
                                filter.getRoomType().toLowerCase()
                        )
                );
            }

            boolean needsFacilityJoin =
                    filter.getWifi() != null ||
                            filter.getWashing() != null ||
                            filter.getAir() != null ||
                            filter.getKitchen() != null;

            if (needsFacilityJoin) {
                Join<Accommodation, Facility> facilityJoin = root.join("facility");

                if (filter.getWifi() != null) {
                    predicates.add(cb.equal(facilityJoin.get("wifi"), filter.getWifi()));
                }

                if (filter.getWashing() != null) {
                    predicates.add(cb.equal(facilityJoin.get("washing"), filter.getWashing()));
                }

                if (filter.getAir() != null) {
                    predicates.add(cb.equal(facilityJoin.get("air"), filter.getAir()));
                }

                if (filter.getKitchen() != null) {
                    predicates.add(cb.equal(facilityJoin.get("kitchen"), filter.getKitchen()));
                }
            }

            if (filter.getCheckIn() != null && filter.getCheckOut() != null) {
                if (!filter.getCheckOut().isAfter(filter.getCheckIn())) {
                    throw new IllegalArgumentException("checkOut must be after checkIn");
                }

                long nights = ChronoUnit.DAYS.between(filter.getCheckIn(), filter.getCheckOut());

                Join<Object, Object> availabilityJoin = root.join("availabilities", JoinType.INNER);

                predicates.add(
                        cb.greaterThanOrEqualTo(
                                availabilityJoin.get("availableDate"),
                                filter.getCheckIn()
                        )
                );

                predicates.add(
                        cb.lessThan(
                                availabilityJoin.get("availableDate"),
                                filter.getCheckOut()
                        )
                );

                predicates.add(
                        cb.isTrue(availabilityJoin.get("isAvailable"))
                );

                query.groupBy(root.get("id"));

                query.having(
                        cb.equal(cb.countDistinct(availabilityJoin.get("availableDate")), nights)
                );
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
