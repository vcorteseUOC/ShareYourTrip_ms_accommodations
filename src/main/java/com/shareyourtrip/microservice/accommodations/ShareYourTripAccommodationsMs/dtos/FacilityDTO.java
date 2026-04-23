package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityDTO {

    private Boolean wifi;
    private Boolean washing;
    private Boolean air;
    private Boolean kitchen;
}
