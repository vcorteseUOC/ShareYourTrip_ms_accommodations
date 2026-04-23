package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isActive;
    private String language;
}
