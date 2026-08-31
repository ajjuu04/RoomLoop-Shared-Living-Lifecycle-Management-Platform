package com.project.roomloop.entity;


import com.project.roomloop.entity.types.MembershipStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    uniqueConstraints ={
            @UniqueConstraint(
                    name = "unique_user_status",
                    columnNames = {"user_id", "membership_status('ACTIVE')"}
            )
    }
)
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name="room_id")
    private Room room;

    @Builder.Default
    private Boolean isAdmin = false;

    @Enumerated(EnumType.STRING)
    private MembershipStatus membershipStatus;
}
