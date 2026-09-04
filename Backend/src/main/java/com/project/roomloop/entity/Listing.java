package com.project.roomloop.entity;

import com.project.roomloop.entity.types.ListingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

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

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime ListedAt;
    
    private LocalDateTime updatedAt;
}
