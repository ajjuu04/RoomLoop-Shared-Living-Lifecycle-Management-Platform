package com.project.roomloop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDate billDate;

    private BigDecimal rentAmount;
    private BigDecimal electricityAmount;
    private BigDecimal otherAmount;
    private String otherDescription;

    private BigDecimal totalAmount;
    private int memberCountAtGeneration;
    private BigDecimal amountPerMember;
}
