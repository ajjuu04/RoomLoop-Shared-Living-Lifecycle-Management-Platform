package com.project.roomloop.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillGenerateRequestDto {
    private BigDecimal electricityAmount;
    private BigDecimal otherAmount;
    private String otherDescription;
}


