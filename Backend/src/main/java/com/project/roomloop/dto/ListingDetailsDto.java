package com.project.roomloop.dto;

import com.project.roomloop.entity.Room;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.ListingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingDetailsDto {
    private Long id;
    private Long room;
    private Long postedBy;
    private int openSpots;
    private String preferences;
    private LocalDateTime listedAt;
    private LocalDateTime updatedAt;
    private ListingStatus listingStatus;
    private String address;
    private BigDecimal rent;
    private BigDecimal deposit;
    private long totalOccupancy;
}
