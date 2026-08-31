package com.project.roomloop.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegisterNewRoomRequest {
    private String address;
    private BigDecimal rent;
    private BigDecimal deposit;
    private long totalOccupancy;
}

