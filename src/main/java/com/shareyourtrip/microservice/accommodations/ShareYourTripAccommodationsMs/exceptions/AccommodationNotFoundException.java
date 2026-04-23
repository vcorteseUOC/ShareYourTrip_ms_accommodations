package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.exceptions;

public class AccommodationNotFoundException extends RuntimeException {

    public AccommodationNotFoundException(String message) {
        super(message);
    }
}
