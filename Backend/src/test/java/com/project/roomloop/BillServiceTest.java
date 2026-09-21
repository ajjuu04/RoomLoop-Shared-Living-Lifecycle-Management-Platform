package com.project.roomloop;

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
import com.project.roomloop.service.BillService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BillServiceTest {

    @InjectMocks
    private BillService billService;

    @Mock
    private BillRepository billRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private HelperForRoomListing helperForRoomListing;

    @Mock
    private RoomAccessGuide roomAccessGuide;


    // =========================================================
    // generateBill()
    // =========================================================

    @Nested
    class GenerateBillTests {

        // this is ahappy a path
        @Test
        void happyPath() {

            Long roomId = 10L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .rent(new BigDecimal("12000"))
                    .build();

            BillGenerateRequestDto request = new BillGenerateRequestDto(
                    new BigDecimal("2000"),
                    new BigDecimal("1000"),
                    "Internet"
            );

            when(helperForRoomListing.checkUser(adminUserId))
                    .thenReturn(admin);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(membershipRepository.countByRoomAndMembershipStatus(
                    room,
                    MembershipStatus.ACTIVE
            )).thenReturn(4L);

            Bill savedBill = Bill.builder()
                    .id(100L)
                    .room(room)
                    .billDate(LocalDate.now())
                    .rentAmount(new BigDecimal("12000"))
                    .electricityAmount(new BigDecimal("2000"))
                    .otherAmount(new BigDecimal("1000"))
                    .otherDescription("Internet")
                    .totalAmount(new BigDecimal("15000"))
                    .memberCountAtGeneration(4)
                    .amountPerMember(new BigDecimal("3750.00"))
                    .build();

            when(billRepository.save(any(Bill.class)))
                    .thenReturn(savedBill);


            BillGenerateResponseDto response =
                    billService.generateBill(roomId, adminUserId, request);


            assertNotNull(response);

            verify(helperForRoomListing)
                    .checkUser(adminUserId);

            verify(roomRepository)
                    .findById(roomId);

            verify(roomAccessGuide)
                    .requireAdmin(admin, room);

            verify(membershipRepository)
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );

            verify(billRepository)
                    .save(any(Bill.class));
        }

        //aI
        @Test
        void adminUserNotFound() {

            Long roomId = 10L;
            Long adminUserId = 1L;

            when(helperForRoomListing.checkUser(adminUserId))
                    .thenThrow(
                            new ResourceNotFoundException(
                                    "User not found with this id"
                            )
                    );


            assertThrows(
                    ResourceNotFoundException.class,
                    () -> billService.generateBill(
                            roomId,
                            adminUserId,
                            new BillGenerateRequestDto()
                    )
            );


            verify(helperForRoomListing)
                    .checkUser(adminUserId);

            verify(roomRepository, never())
                    .findById(anyLong());

            verify(billRepository, never())
                    .save(any(Bill.class));
        }


        @Test
        void roomNotFound() {

            Long roomId = 10L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            when(helperForRoomListing.checkUser(adminUserId))
                    .thenReturn(admin);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.empty());


            assertThrows(
                    ResourceNotFoundException.class,
                    () -> billService.generateBill(
                            roomId,
                            adminUserId,
                            new BillGenerateRequestDto()
                    )
            );


            verify(helperForRoomListing)
                    .checkUser(adminUserId);

            verify(roomRepository)
                    .findById(roomId);

            verify(roomAccessGuide, never())
                    .requireAdmin(any(User.class), any(Room.class));

            verify(billRepository, never())
                    .save(any(Bill.class));
        }


        @Test
        void noActiveMembers() {

            Long roomId = 10L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .rent(new BigDecimal("12000"))
                    .build();

            when(helperForRoomListing.checkUser(adminUserId))
                    .thenReturn(admin);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(membershipRepository.countByRoomAndMembershipStatus(
                    room,
                    MembershipStatus.ACTIVE
            )).thenReturn(0L);


            assertThrows(
                    IllegalStateException.class,
                    () -> billService.generateBill(
                            roomId,
                            adminUserId,
                            new BillGenerateRequestDto()
                    )
            );


            verify(helperForRoomListing)
                    .checkUser(adminUserId);

            verify(roomRepository)
                    .findById(roomId);

            verify(roomAccessGuide)
                    .requireAdmin(admin, room);

            verify(membershipRepository)
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );

            verify(billRepository, never())
                    .save(any(Bill.class));
        }


        @Test
        void verifyBillCalculation() {

            Long roomId = 10L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .rent(new BigDecimal("12000"))
                    .build();

            BillGenerateRequestDto request =
                    new BillGenerateRequestDto(
                            new BigDecimal("2000"),
                            new BigDecimal("1000"),
                            "Internet"
                    );

            when(helperForRoomListing.checkUser(adminUserId))
                    .thenReturn(admin);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(membershipRepository.countByRoomAndMembershipStatus(
                    room,
                    MembershipStatus.ACTIVE
            )).thenReturn(4L);

            Bill savedBill = Bill.builder()
                    .id(100L)
                    .room(room)
                    .build();

            when(billRepository.save(any(Bill.class)))
                    .thenReturn(savedBill);


            billService.generateBill(
                    roomId,
                    adminUserId,
                    request
            );


            ArgumentCaptor<Bill> billCaptor =
                    ArgumentCaptor.forClass(Bill.class);

            verify(billRepository)
                    .save(billCaptor.capture());

            Bill bill = billCaptor.getValue();


            assertEquals(
                    new BigDecimal("12000"),
                    bill.getRentAmount()
            );

            assertEquals(
                    new BigDecimal("2000"),
                    bill.getElectricityAmount()
            );

            assertEquals(
                    new BigDecimal("1000"),
                    bill.getOtherAmount()
            );

            assertEquals(
                    new BigDecimal("15000"),
                    bill.getTotalAmount()
            );

            assertEquals(
                    4,
                    bill.getMemberCountAtGeneration()
            );

            assertEquals(
                    new BigDecimal("3750.00"),
                    bill.getAmountPerMember()
            );

            assertEquals(
                    "Internet",
                    bill.getOtherDescription()
            );

            assertEquals(
                    room,
                    bill.getRoom()
            );
        }


        @Test
        void nullElectricityAndOtherAmountShouldBeTreatedAsZero() {

            Long roomId = 10L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .rent(new BigDecimal("12000"))
                    .build();

            BillGenerateRequestDto request =
                    new BillGenerateRequestDto(
                            null,
                            null,
                            null
                    );

            when(helperForRoomListing.checkUser(adminUserId))
                    .thenReturn(admin);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(membershipRepository.countByRoomAndMembershipStatus(
                    room,
                    MembershipStatus.ACTIVE
            )).thenReturn(4L);

            when(billRepository.save(any(Bill.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));


            billService.generateBill(
                    roomId,
                    adminUserId,
                    request
            );


            ArgumentCaptor<Bill> billCaptor =
                    ArgumentCaptor.forClass(Bill.class);

            verify(billRepository)
                    .save(billCaptor.capture());

            Bill bill = billCaptor.getValue();


            assertEquals(
                    BigDecimal.ZERO,
                    bill.getElectricityAmount()
            );

            assertEquals(
                    BigDecimal.ZERO,
                    bill.getOtherAmount()
            );

            assertEquals(
                    new BigDecimal("12000"),
                    bill.getTotalAmount()
            );

            assertEquals(
                    new BigDecimal("3000.00"),
                    bill.getAmountPerMember()
            );
        }
    }



    @Nested
    class GetBillHistoryTests {
    // this is hjappy path
        @Test
        void GetBillHistoryTests_happyPath() {

            Long roomId = 10L;
            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            Bill bill1 = Bill.builder()
                    .id(100L)
                    .room(room)
                    .billDate(LocalDate.of(2026, 9, 20))
                    .rentAmount(new BigDecimal("12000"))
                    .electricityAmount(new BigDecimal("2000"))
                    .otherAmount(new BigDecimal("1000"))
                    .otherDescription("Internet")
                    .totalAmount(new BigDecimal("15000"))
                    .memberCountAtGeneration(4)
                    .amountPerMember(new BigDecimal("3750.00"))
                    .build();

            Bill bill2 = Bill.builder()
                    .id(101L)
                    .room(room)
                    .billDate(LocalDate.of(2026, 9, 10))
                    .rentAmount(new BigDecimal("12000"))
                    .electricityAmount(new BigDecimal("1500"))
                    .otherAmount(new BigDecimal("500"))
                    .otherDescription("Gas")
                    .totalAmount(new BigDecimal("14000"))
                    .memberCountAtGeneration(4)
                    .amountPerMember(new BigDecimal("3500.00"))
                    .build();


            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(billRepository.findByRoomOrderByBillDateDesc(room))
                    .thenReturn(List.of(bill1, bill2));


            List<BillGenerateResponseDto> response =
                    billService.getBillHistory(roomId, userId);


            assertNotNull(response);

            assertEquals(2, response.size());


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(roomRepository)
                    .findById(roomId);

            verify(roomAccessGuide)
                    .isUserActiveMemberOfRoom(user, room);

            verify(billRepository)
                    .findByRoomOrderByBillDateDesc(room);
        }

        // AI
        @Test
        void userNotFound() {

            Long roomId = 10L;
            Long userId = 1L;

            when(helperForRoomListing.checkUser(userId))
                    .thenThrow(
                            new ResourceNotFoundException(
                                    "User not found with this id"
                            )
                    );


            assertThrows(
                    ResourceNotFoundException.class,
                    () -> billService.getBillHistory(
                            roomId,
                            userId
                    )
            );


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(roomRepository, never())
                    .findById(anyLong());

            verify(billRepository, never())
                    .findByRoomOrderByBillDateDesc(any(Room.class));
        }


        @Test
        void roomNotFound() {

            Long roomId = 10L;
            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.empty());


            assertThrows(
                    ResourceNotFoundException.class,
                    () -> billService.getBillHistory(
                            roomId,
                            userId
                    )
            );


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(roomRepository)
                    .findById(roomId);

            verify(roomAccessGuide, never())
                    .isUserActiveMemberOfRoom(
                            any(User.class),
                            any(Room.class)
                    );

            verify(billRepository, never())
                    .findByRoomOrderByBillDateDesc(any(Room.class));
        }


        @Test
        void userIsNotActiveMember() {

            Long roomId = 10L;
            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            doThrow(
                    new IllegalStateException(
                            "User is not an active member of this room"
                    )
            ).when(roomAccessGuide)
                    .isUserActiveMemberOfRoom(user, room);


            assertThrows(
                    IllegalStateException.class,
                    () -> billService.getBillHistory(
                            roomId,
                            userId
                    )
            );


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(roomRepository)
                    .findById(roomId);

            verify(roomAccessGuide)
                    .isUserActiveMemberOfRoom(user, room);

            verify(billRepository, never())
                    .findByRoomOrderByBillDateDesc(any(Room.class));
        }
    }
}