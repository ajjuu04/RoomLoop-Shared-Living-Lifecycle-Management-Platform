package com.project.roomloop.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailsDto {
    private Long id;
    private String address;
    private BigDecimal rent;
    private BigDecimal deposit;
    private long totalOccupancy;
    private Long cretedByUser;
}

