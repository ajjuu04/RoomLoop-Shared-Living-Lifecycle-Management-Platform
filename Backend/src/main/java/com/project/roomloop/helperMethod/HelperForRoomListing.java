package com.project.roomloop.helperMethod;

import com.project.roomloop.entity.Room;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.MembershipStatus;
import com.project.roomloop.repository.MembershipRepository;
import com.project.roomloop.repository.RoomRepository;
import com.project.roomloop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class HelperForRoomListing {

    private final RoomRepository roomRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public BigDecimal getCurrentRentPerMemberForRoom(Room room) {
        long activeMembers = membershipRepository
                .countByRoomAndMembershipStatus(room, MembershipStatus.ACTIVE);

        if (activeMembers == 0) {
            throw new IllegalStateException("Room has no active members please crete new Room with details");
        }

        return room.getRent().divide(BigDecimal.valueOf(activeMembers),2);
    }

    public BigDecimal getCurrentDepositPerMemberForRoom(Room room) {
        long activeMembers = membershipRepository
                .countByRoomAndMembershipStatus(room, MembershipStatus.ACTIVE);

        if (activeMembers == 0) {
            throw new IllegalStateException("Room has no active members please crete new Room with details");
        }

        return room.getDeposit().divide(BigDecimal.valueOf(activeMembers),2);
    }


    public BigDecimal getCurrentRentPerMemberForListing(Room room) {
        return room.getRent().divide(BigDecimal.valueOf(room.getTotalOccupancy()), 2);
    }

    public BigDecimal getCurrentDepositPerMemberForListing(Room room) {
        return room.getDeposit().divide(BigDecimal.valueOf(room.getTotalOccupancy()), 2);
    }

    public User checkUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with this id"));
    }
}


