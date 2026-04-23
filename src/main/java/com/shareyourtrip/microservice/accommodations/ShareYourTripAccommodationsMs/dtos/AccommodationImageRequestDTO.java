package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationImageRequestDTO {

    private String imageUrl;
    private Boolean isCover;
}
