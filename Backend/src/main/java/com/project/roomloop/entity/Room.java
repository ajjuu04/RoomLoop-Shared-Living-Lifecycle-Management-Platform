package com.project.roomloop.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @Size(min = 8, max = 360,message = "Address should be in proper manner between 8 to 360 characters")
    @NotNull(message = "Address cant be null")
    private String address;

    @DecimalMin(value = "100", message = "rent always should be above 100")
    @NotNull(message = "Rent cant be null")
    private BigDecimal rent;

    @DecimalMin(value = "0", message = "Deposit always should be Positive")
    @NotNull(message = "Deposit cant be null")
    private BigDecimal deposit;

    @Min(value = 1, message = "Total occupancy always should more that 1")
    @Max(value = 50, message = "Total occupancy always should less that 50")
    @NotNull(message = "room occupancy cant be null")
    @Column(nullable = false)
    private long totalOccupancy;

    @NotNull
    @ManyToOne
    private User cretedBy;
}
