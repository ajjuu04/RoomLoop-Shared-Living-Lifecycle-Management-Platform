package com.project.roomloop.service;

import com.project.roomloop.dto.BillGenerateRequestDto;
import com.project.roomloop.dto.BillGenerateResponseDto;
import com.project.roomloop.entity.Bill;
import com.project.roomloop.entity.Room;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.MembershipStatus;
import com.project.roomloop.error.ResourceNotFoundException;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.helperMethod.RoomAccessGuide;
import com.project.roomloop.repository.BillRepository;
import com.project.roomloop.repository.MembershipRepository;
import com.project.roomloop.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final RoomRepository roomRepository;
    private final MembershipRepository membershipRepository;
    private final HelperForRoomListing helperForRoomListing;
    private final RoomAccessGuide roomAccessGuide;

    @Transactional
    public BillGenerateResponseDto generateBill(Long roomId, Long adminUserId, BillGenerateRequestDto request) {
        User admin = helperForRoomListing.checkUser(adminUserId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        roomAccessGuide.requireAdmin(admin, room);

        long activeMembers = membershipRepository.countByRoomAndMembershipStatus(room, MembershipStatus.ACTIVE);
        if (activeMembers == 0) {
            throw new IllegalStateException("No active members to split this bill across");
        }

        BigDecimal electricity = request.getElectricityAmount() != null ? request.getElectricityAmount() : BigDecimal.ZERO;
        BigDecimal other = request.getOtherAmount() != null ? request.getOtherAmount() : BigDecimal.ZERO;
        BigDecimal total = room.getRent().add(electricity).add(other);
        BigDecimal perMember = total.divide(BigDecimal.valueOf(activeMembers),2,RoundingMode.HALF_UP);

        Bill bill = billRepository.save(Bill.builder()
                .room(room)
                .billDate(LocalDate.now())
                .rentAmount(room.getRent())
                .electricityAmount(electricity)
                .otherAmount(other)
                .otherDescription(request.getOtherDescription())
                .totalAmount(total)
                .memberCountAtGeneration((int) activeMembers)
                .amountPerMember(perMember)
                .build());

        return mapToDto(bill);
    }

    public List<BillGenerateResponseDto> getBillHistory(Long roomId, Long userId) {
        User user = helperForRoomListing.checkUser(userId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        roomAccessGuide.isUserActiveMemberOfRoom(user, room);

        return billRepository.findByRoomOrderByBillDateDesc(room).stream()
                .map(this::mapToDto)
                .toList();
    }

    private BillGenerateResponseDto mapToDto(Bill bill) {
        return new BillGenerateResponseDto(bill.getId(), bill.getBillDate(), bill.getRentAmount(),
                bill.getElectricityAmount(), bill.getOtherAmount(), bill.getOtherDescription(),
                bill.getTotalAmount(), bill.getMemberCountAtGeneration(), bill.getAmountPerMember());
    }
}
