package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.mappers;

import com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites.AccommodationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AccommodationStatusConverter implements AttributeConverter<AccommodationStatus, String> {

    @Override
    public String convertToDatabaseColumn(AccommodationStatus attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public AccommodationStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AccommodationStatus.valueOf(dbData.toUpperCase());
    }
}