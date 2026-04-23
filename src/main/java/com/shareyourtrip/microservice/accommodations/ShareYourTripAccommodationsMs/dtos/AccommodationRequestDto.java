package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationRequestDto {
    @NotNull
    private Long hostId;

    @NotBlank
    private String title;
    @NotBlank
    private String description;

    private String addressLine;
    private String city;
    private String country;
    private String postalCode;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal pricePerNight;
    @NotNull
    @Min(1)
    private Integer maxGuests;
    private String roomType;
    private String rules;

    private FacilityDTO facilities;
    private List<AccommodationImageRequestDTO> images;
    private List<LocalDate> availableDates;
}