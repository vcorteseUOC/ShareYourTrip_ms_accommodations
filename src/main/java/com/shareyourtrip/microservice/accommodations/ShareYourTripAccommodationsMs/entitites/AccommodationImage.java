package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "accommodation_images", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "accommodation")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AccommodationImage {

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

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "is_cover", nullable = false)
    @Builder.Default
    private Boolean isCover = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
