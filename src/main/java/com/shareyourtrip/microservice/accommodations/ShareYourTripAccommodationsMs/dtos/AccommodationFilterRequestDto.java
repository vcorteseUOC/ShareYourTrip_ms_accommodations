package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationFilterRequestDto {
    private String city;
    private String country;
    private Long hostId;
    private String language;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer guests;
    private String roomType;
    private Boolean wifi;
    private Boolean washing;
    private Boolean air;
    private Boolean kitchen;
    private LocalDate checkIn;
    private LocalDate checkOut;
    // hostIds resueltos externamente desde el microservicio de usuarios
    private List<Long> hostIds;
}
