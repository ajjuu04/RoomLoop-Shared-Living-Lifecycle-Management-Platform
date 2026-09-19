package com.project.roomloop.service;

import com.project.roomloop.dto.*;
import com.project.roomloop.entity.Membership;
import com.project.roomloop.entity.Room;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.MembershipStatus;
import com.project.roomloop.error.ActiveMembershipException;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.helperMethod.RoomAccessGuide;
import com.project.roomloop.repository.MembershipRepository;
import com.project.roomloop.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static org.apache.el.lang.ELArithmetic.divide;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final MembershipRepository membershipRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final RoomAccessGuide roomAccessGuide;
    private final HelperForRoomListing helperForRoomListing;


    @Transactional
    public RoomDetailsDto registerNewRoom(RegisterNewRoomRequest registerNewRoomRequest, Long userId) {

        User user = helperForRoomListing.checkUser(userId);

        boolean isOtherRoomMember =
                membershipRepository.existsByUserAndMembershipStatus(
                        user, MembershipStatus.ACTIVE
                );

        if (isOtherRoomMember) {
            throw new ActiveMembershipException(
                    "That user have alredy active in Another room, Exits from that room first"
            );
        }

        Room room = Room.builder()
                .address(registerNewRoomRequest.getAddress())
                .rent(registerNewRoomRequest.getRent())
                .deposit(registerNewRoomRequest.getDeposit())
                .totalOccupancy(registerNewRoomRequest.getTotalOccupancy())
                .cretedBy(user)
                .build();

        Room savedRoom = roomRepository.save(room);

        Membership membership = Membership.builder()
                .user(user)
                .room(savedRoom)
                .isAdmin(true)
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();

        membershipRepository.save(membership);

        return RoomDetailsDto.builder()
                .id(savedRoom.getId())
                .address(savedRoom.getAddress())
                .rent(savedRoom.getRent())
                .deposit(savedRoom.getDeposit())
                .totalOccupancy(savedRoom.getTotalOccupancy())
                .cretedByUser(savedRoom.getCretedBy().getId())
                .build();
    }


    public RoomDetailsDto getRoomDetails(Long userId) {

        User user = helperForRoomListing.checkUser(userId);

        Membership membership =
                membershipRepository.findByUserAndMembershipStatus(
                        user, MembershipStatus.ACTIVE
                ).orElseThrow(() ->
                        new ActiveMembershipException(
                                "This user Dont have Any Active Membership in any room, create Or Join Room"
                        )
                );

        Room room = membership.getRoom();

        return RoomDetailsDto.builder()
                .id(room.getId())
                .address(room.getAddress())
                .rent(room.getRent())
                .deposit(room.getDeposit())
                .totalOccupancy(room.getTotalOccupancy())
                .cretedByUser(room.getCretedBy().getId())
                .build();
    }


    public RoomDetailsDto getRoomDetailsById(Long id) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "There in no any Room with this Id"
                        )
                );

        return RoomDetailsDto.builder()
                .id(room.getId())
                .address(room.getAddress())
                .rent(room.getRent())
                .deposit(room.getDeposit())
                .totalOccupancy(room.getTotalOccupancy())
                .cretedByUser(room.getCretedBy().getId())
                .build();
    }


    @Transactional
    public RoomDetailsDto editRoom(
            Long roomId,
            RoomUpdateDto roomUpdateDto,
            Long userId
    ) {

        User user = helperForRoomListing.checkUser(userId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "room not Found with this Id"
                        )
                );

        roomAccessGuide.isUserAdmin(user, room);

        room.setAddress(roomUpdateDto.getAddress());
        room.setRent(roomUpdateDto.getRent());
        room.setDeposit(roomUpdateDto.getDeposit());

        Room updatedRoom = roomRepository.save(room);

        return modelMapper.map(updatedRoom, RoomDetailsDto.class);
    }


    @Transactional
    public RoomDetailsDto editRoomOccupancy(
            Long roomId,
            RoomOccupancyUpdateDto roomOccupancyUpdateDto,
            Long userId
    ) {

        User user = helperForRoomListing.checkUser(userId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "room not Found with this Id"
                        )
                );

        roomAccessGuide.isUserAdmin(user, room);

        long currentActiveMemberInRoom =
                membershipRepository.countByRoomAndMembershipStatus(
                        room, MembershipStatus.ACTIVE
                );

        log.info("currentActiveMemberInRoom is : " + currentActiveMemberInRoom);

        long newTotalOccupancy =
                roomOccupancyUpdateDto.getTotalOccupancy();

        log.info("newTotalOccupancy is : " + newTotalOccupancy);

        if (newTotalOccupancy < currentActiveMemberInRoom) {
            throw new IllegalArgumentException(
                    "Cannot reduce the Occupency cause we fo that u have to remove old member first, current Occupency is : "
                            + currentActiveMemberInRoom
            );
        }

        room.setTotalOccupancy(newTotalOccupancy);
        roomRepository.save(room);

        return RoomDetailsDto.builder()
                .id(room.getId())
                .address(room.getAddress())
                .rent(room.getRent())
                .deposit(room.getDeposit())
                .totalOccupancy(newTotalOccupancy)
                .cretedByUser(room.getCretedBy().getId())
                .build();
    }


    public BillsGenerateResponse generateMonthlyBill(Long memberId, Long roomId) {
        User user = helperForRoomListing.checkUser(memberId);
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("There is no Room found with hthis id"));

        roomAccessGuide.isUserActiveMemberOfRoom(user,room);
        Long currentActiveMembers = membershipRepository.countByRoomAndMembershipStatus(room,MembershipStatus.ACTIVE);

        return new BillsGenerateResponse((BigDecimal) divide(room.getRent(),currentActiveMembers));
    }
}