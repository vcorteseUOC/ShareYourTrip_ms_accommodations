package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityDto {
    private Long id;
    private LocalDate availableDate;
    private Boolean isAvailable;
}
