package com.shareyourtrip.microservice.accommodations.ShareYourTripAccommodationsMs.entitites;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facilities", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "accommodation")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Facility {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(
            name = "id",
            foreignKey = @ForeignKey(name = "fk_facilities_accommodation")
    )
    private Accommodation accommodation;

    @Column(name = "wifi")
    private Boolean wifi;

    @Column(name = "washing")
    private Boolean washing;

    @Column(name = "air")
    private Boolean air;

    @Column(name = "kitchen")
    private Boolean kitchen;
}
