package com.project.roomloop.entity;

import com.project.roomloop.entity.types.ListingStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Room room;

    @ManyToOne
    private User postedBy;

    private int openSpots;

    private String preferences;

    @Enumerated(EnumType.STRING)
    private ListingStatus listingStatus;
}
