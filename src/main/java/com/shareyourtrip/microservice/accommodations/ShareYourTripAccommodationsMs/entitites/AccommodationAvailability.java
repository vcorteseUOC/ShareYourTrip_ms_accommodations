package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "accommodation_availability",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_accommodation_availability",
                        columnNames = {"accommodation_id", "available_date"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "accommodation")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AccommodationAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "accommodation_id",
            nullable = false
    )
    private Accommodation accommodation;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;
}
