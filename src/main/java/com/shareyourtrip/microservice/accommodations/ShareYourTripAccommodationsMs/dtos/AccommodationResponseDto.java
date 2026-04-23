package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.AccommodationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationResponseDto {

    private Long id;

    // Host
    private Long hostId;

    // Info básica
    private String title;
    private String description;

    // Dirección
    private String addressLine;
    private String city;
    private String country;
    private String postalCode;

    private BigDecimal latitude;
    private BigDecimal longitude;

    // Pricing
    private BigDecimal pricePerNight;
    private Integer maxGuests;

    private String roomType;
    private AccommodationStatus status;

    // Extras
    private List<String> imageUrls;
    private String coverImage;

    private FacilityDTO facilities;
}