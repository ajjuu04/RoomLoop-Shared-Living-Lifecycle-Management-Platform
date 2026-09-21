package com.project.roomloop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillGenerateResponseDto {
    private Long id;
    private LocalDate billDate;
    private BigDecimal rentAmount;
    private BigDecimal electricityAmount;
    private BigDecimal otherAmount;
    private String otherDescription;
    private BigDecimal totalAmount;
    private int memberCountAtGeneration;
    private BigDecimal amountPerMember;
}
