package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.exceptions;

public final class Messages {
    public static final String ACCOMMODATION_NOT_FOUND = "Residencia no encontrada";
    public static final String ACCOMMODATION_NOT_FOUND_PREFIX = "Accommodation not found with id: ";
    public static final String CHECKIN_CHECKOUT_REQUIRED_TOGETHER = "checkIn and checkOut must be provided together";
    public static final String CHECKOUT_AFTER_CHECKIN = "checkOut must be after checkIn";
    public static final String USER_NOT_FOUND_PREFIX = "User not found with id: ";
    public static final String ERROR_CALLING_USER_SERVICE = "Error calling user service";
    public static final String USER_NOT_ACTIVE_PREFIX = "User is not active: ";
    public static final String ONLY_ONE_COVER_IMAGE = "Only one image can be marked as cover";
}
