package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
