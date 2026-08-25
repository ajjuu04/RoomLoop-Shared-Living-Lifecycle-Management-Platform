package com.project.roomloop.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    private BigDecimal rent;

    private BigDecimal deposit;

    @Min(value = 1,message = "Total occupency always should more that 1")
    @NotNull
    @Column(nullable = false)
    private int totalOccupancy;

    @NotNull
    @ManyToOne
    private User cretedBy;
}
