package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.mappers;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationImageRequestDTO;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationRequestDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.AccommodationResponseDto;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos.FacilityDTO;
import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AccommodationMapper {

    public static AccommodationResponseDto toDTO(Accommodation entity) {

        if (entity == null) return null;

        return AccommodationResponseDto.builder()
                .id(entity.getId())

                // Host
                .hostId(entity.getHostId())

                // Basic
                .title(entity.getTitle())
                .description(entity.getDescription())

                // Address
                .addressLine(entity.getAddressLine())
                .city(entity.getCity())
                .country(entity.getCountry())
                .postalCode(entity.getPostalCode())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())

                // Pricing
                .pricePerNight(entity.getPricePerNight())
                .maxGuests(entity.getMaxGuests())
                .roomType(entity.getRoomType())
                .status(entity.getStatus())

                // Images
                .imageUrls(mapImages(entity.getImages()))
                .coverImage(findCover(entity.getImages()))

                // Facilities
                .facilities(mapFacilities(entity.getFacility()))

                .build();
    }

    public static Accommodation toEntity(AccommodationRequestDto request) {
        LocalDateTime now = LocalDateTime.now();
        Accommodation accommodation = Accommodation.builder()
                .hostId(request.getHostId())
                .title(request.getTitle())
                .description(request.getDescription())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .pricePerNight(request.getPricePerNight())
                .maxGuests(request.getMaxGuests())
                .roomType(request.getRoomType())
                .rules(request.getRules())
                .status(AccommodationStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        if (request.getFacilities() != null) {
            Facility facility = Facility.builder()
                    .accommodation(accommodation)
                    .wifi(request.getFacilities().getWifi())
                    .washing(request.getFacilities().getWashing())
                    .air(request.getFacilities().getAir())
                    .kitchen(request.getFacilities().getKitchen())
                    .build();

            accommodation.setFacility(facility);
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<AccommodationImage> images = new ArrayList<>();

            for (AccommodationImageRequestDTO imageRequest : request.getImages()) {
                AccommodationImage image = AccommodationImage.builder()
                        .accommodation(accommodation)
                        .imageUrl(imageRequest.getImageUrl())
                        .isCover(Boolean.TRUE.equals(imageRequest.getIsCover()))
                        .createdAt(now)
                        .build();

                images.add(image);
            }

            accommodation.setImages(images);
        }

        if (request.getAvailableDates() != null && !request.getAvailableDates().isEmpty()) {
            List<AccommodationAvailability> availabilities = request.getAvailableDates()
                    .stream()
                    .distinct()
                    .map(date -> AccommodationAvailability.builder()
                            .accommodation(accommodation)
                            .availableDate(date)
                            .isAvailable(true)
                            .build())
                    .toList();

            accommodation.setAvailabilities(new ArrayList<>(availabilities));
        }
        return accommodation;
    }

    private static List<String> mapImages(List<AccommodationImage> images) {
        if (images == null) return List.of();

        return images.stream()
                .map(AccommodationImage::getImageUrl)
                .collect(Collectors.toList());
    }

    private static String findCover(List<AccommodationImage> images) {
        if (images == null) return null;

        return images.stream()
                .filter(AccommodationImage::getIsCover)
                .map(AccommodationImage::getImageUrl)
                .findFirst()
                .orElse(null);
    }

    private static FacilityDTO mapFacilities(Facility facility) {
        if (facility == null) return null;

        return FacilityDTO.builder()
                .wifi(facility.getWifi())
                .washing(facility.getWashing())
                .air(facility.getAir())
                .kitchen(facility.getKitchen())
                .build();
    }
}
