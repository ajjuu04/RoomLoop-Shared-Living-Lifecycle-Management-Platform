package com.project.roomloop.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomUpdateDto {
    private String address;
    private BigDecimal rent;
    private BigDecimal deposit;
}
