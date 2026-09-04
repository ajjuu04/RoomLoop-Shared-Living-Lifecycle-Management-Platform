package com.project.roomloop;


import com.project.roomloop.dto.RegisterNewRoomRequest;
import com.project.roomloop.dto.RoomDetailsDto;
import com.project.roomloop.dto.RoomOccupancyUpdateDto;
import com.project.roomloop.dto.RoomUpdateDto;
import com.project.roomloop.entity.Membership;
import com.project.roomloop.entity.Room;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.MembershipStatus;
import com.project.roomloop.error.ActiveMembershipException;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.helperMethod.RoomAccessGuide;
import com.project.roomloop.repository.MembershipRepository;
import com.project.roomloop.repository.RoomRepository;
import com.project.roomloop.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAccessGuide roomAccessGuide;

    @Mock
    private HelperForRoomListing helperForRoomListing;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RoomService roomService;


    @Nested
    class registerNewRoom {

        @Test
        public void registerNewRoomIfUserNotActiveGoodPathTest() {

            RegisterNewRoomRequest request = new RegisterNewRoomRequest();
            request.setAddress("nana Peth");
            request.setRent(new BigDecimal("12000"));
            request.setDeposit(new BigDecimal("24000"));
            request.setTotalOccupancy(4);

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room savedRoom = Room.builder()
                    .id(10L)
                    .address("nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .room(savedRoom)
                    .isAdmin(true)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(membershipRepository.existsByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(false);

            when(roomRepository.save(any(Room.class)))
                    .thenReturn(savedRoom);

            when(membershipRepository.save(any(Membership.class)))
                    .thenReturn(membership);

            RoomDetailsDto result =
                    roomService.registerNewRoom(request, userId);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());
        }


        @Test
        public void registerNewRoomIfUserIsActiveInAnotherRoomTest() {

            RegisterNewRoomRequest request = new RegisterNewRoomRequest();
            request.setAddress("nana Peth");
            request.setRent(new BigDecimal("12000"));
            request.setDeposit(new BigDecimal("24000"));
            request.setTotalOccupancy(4);

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(membershipRepository.existsByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(true);

            assertThrows(
                    ActiveMembershipException.class,
                    () -> roomService.registerNewRoom(request, userId)
            );

            verify(roomRepository, never()).save(any(Room.class));
            verify(membershipRepository, never()).save(any(Membership.class));
        }


        @Test
        public void registerNewRoom_ShouldCreateCorrectRoom() {

            RegisterNewRoomRequest request = new RegisterNewRoomRequest();

            request.setAddress("nana Peth");
            request.setRent(new BigDecimal("12000"));
            request.setDeposit(new BigDecimal("24000"));
            request.setTotalOccupancy(4);

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room savedRoom = Room.builder()
                    .id(10L)
                    .address("nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(membershipRepository.existsByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(false);

            when(roomRepository.save(any(Room.class)))
                    .thenReturn(savedRoom);

            when(membershipRepository.save(any(Membership.class)))
                    .thenReturn(
                            Membership.builder()
                                    .user(user)
                                    .room(savedRoom)
                                    .isAdmin(true)
                                    .membershipStatus(MembershipStatus.ACTIVE)
                                    .build()
                    );

            RoomDetailsDto result =
                    roomService.registerNewRoom(request, userId);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());

            verify(roomRepository).save(argThat(room ->
                    room.getAddress().equals("nana Peth")
                            && room.getRent().compareTo(new BigDecimal("12000")) == 0
                            && room.getDeposit().compareTo(new BigDecimal("24000")) == 0
                            && room.getTotalOccupancy() == 4
                            && room.getCretedBy().equals(user)
            ));
        }


        @Test
        public void registerNewRoom_ShouldCreateCorrectMembership() {

            RegisterNewRoomRequest request = new RegisterNewRoomRequest();

            request.setAddress("nana Peth");
            request.setRent(new BigDecimal("12000"));
            request.setDeposit(new BigDecimal("24000"));
            request.setTotalOccupancy(4);

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room savedRoom = Room.builder()
                    .id(10L)
                    .address("nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(membershipRepository.existsByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(false);

            when(roomRepository.save(any(Room.class)))
                    .thenReturn(savedRoom);

            when(membershipRepository.save(any(Membership.class)))
                    .thenReturn(
                            Membership.builder()
                                    .user(user)
                                    .room(savedRoom)
                                    .isAdmin(true)
                                    .membershipStatus(MembershipStatus.ACTIVE)
                                    .build()
                    );

            RoomDetailsDto result =
                    roomService.registerNewRoom(request, userId);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());

            verify(membershipRepository).save(argThat(membership ->
                    membership.getUser().equals(user)
                            && membership.getRoom().equals(savedRoom)
                            && membership.getIsAdmin()
                            && membership.getMembershipStatus() == MembershipStatus.ACTIVE
            ));
        }


        @Test
        public void registerNewRoom_ShouldThrowException_WhenRoomSaveFails() {

            RegisterNewRoomRequest request = new RegisterNewRoomRequest();

            request.setAddress("nana Peth");
            request.setRent(new BigDecimal("12000"));
            request.setDeposit(new BigDecimal("24000"));
            request.setTotalOccupancy(4);

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(membershipRepository.existsByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(false);

            when(roomRepository.save(any(Room.class)))
                    .thenThrow(new RuntimeException("Database error"));

            assertThrows(
                    RuntimeException.class,
                    () -> roomService.registerNewRoom(request, userId)
            );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }
    }


    @Nested
    class getRoomDetails {

        @Test
        public void getRoomDetails_ShouldReturnRoomDetails() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .room(room)
                    .isAdmin(true)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(membershipRepository.findByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(Optional.of(membership));

            RoomDetailsDto result =
                    roomService.getRoomDetails(userId);

            assertEquals(10L, result.getId());
            assertEquals("Nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());
        }


        @Test
        public void getRoomDetails_ShouldThrowException_WhenUserHasNoActiveMembership() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(membershipRepository.findByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(Optional.empty());

            assertThrows(
                    ActiveMembershipException.class,
                    () -> roomService.getRoomDetails(userId)
            );
        }


        @Test
        public void getRoomDetails_ShouldReturnRoomDetailsVarifyRepo() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .room(room)
                    .isAdmin(true)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(membershipRepository.findByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(Optional.of(membership));

            RoomDetailsDto result =
                    roomService.getRoomDetails(userId);

            assertEquals(10L, result.getId());
            assertEquals("Nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());

            verify(membershipRepository)
                    .findByUserAndMembershipStatus(
                            user,
                            MembershipStatus.ACTIVE
                    );
        }
    }


    @Nested
    class getRoomDetailsById {

        @Test
        public void getRoomDetailsById_ShouldReturnRoomDetails() {

            User user = User.builder()
                    .id(1L)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            RoomDetailsDto result =
                    roomService.getRoomDetailsById(10L);

            assertEquals(10L, result.getId());
            assertEquals("Nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());
        }


        @Test
        public void getRoomDetailsById_ShouldThrowException_WhenRoomNotFound() {

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> roomService.getRoomDetailsById(10L)
            );

            verify(roomRepository).findById(10L);
        }


    }


    @Nested
    class editRoomDetails {

        @Test
        public void editRoom_ShouldUpdateRoomAndReturnDto() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            RoomUpdateDto request = new RoomUpdateDto();
            request.setAddress("Kothrud, Pune");
            request.setRent(new BigDecimal("40000"));
            request.setDeposit(new BigDecimal("80000"));

            RoomDetailsDto expectedDto = RoomDetailsDto.builder()
                    .id(10L)
                    .address("Kothrud, Pune")
                    .rent(new BigDecimal("40000"))
                    .deposit(new BigDecimal("80000"))
                    .totalOccupancy(4)
                    .cretedByUser(1L)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomRepository.save(any(Room.class)))
                    .thenReturn(room);

            when(modelMapper.map(room, RoomDetailsDto.class))
                    .thenReturn(expectedDto);

            RoomDetailsDto result =
                    roomService.editRoom(10L, request, userId);

            assertEquals(10L, result.getId());
            assertEquals("Kothrud, Pune", result.getAddress());
            assertEquals(new BigDecimal("40000"), result.getRent());
            assertEquals(new BigDecimal("80000"), result.getDeposit());

            verify(roomAccessGuide)
                    .isUserAdmin(user, room);

            verify(roomRepository)
                    .save(room);
        }


        @Test
        public void editRoom_ShouldThrowException_WhenRoomNotFound() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            RoomUpdateDto request = new RoomUpdateDto();
            request.setAddress("Kothrud, Pune");
            request.setRent(new BigDecimal("40000"));
            request.setDeposit(new BigDecimal("80000"));

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> roomService.editRoom(10L, request, userId)
            );

            verify(roomRepository, never())
                    .save(any(Room.class));

            verify(roomAccessGuide, never())
                    .isUserAdmin(any(User.class), any(Room.class));
        }


        @Test
        public void editRoom_ShouldThrowException_WhenUserIsNotAdmin() {

            Long userId = 2L;

            User user = User.builder()
                    .id(userId)
                    .name("Rahul")
                    .email("rahul@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            RoomUpdateDto request = new RoomUpdateDto();
            request.setAddress("Kothrud, Pune");
            request.setRent(new BigDecimal("40000"));
            request.setDeposit(new BigDecimal("80000"));

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            doThrow(new AccessDeniedException(
                    "Only admin can edit room"
            )).when(roomAccessGuide)
                    .isUserAdmin(user, room);

            assertThrows(
                    AccessDeniedException.class,
                    () -> roomService.editRoom(10L, request, userId)
            );

            verify(roomRepository, never())
                    .save(any(Room.class));
        }


        @Test
        public void editRoom_ShouldThrowException_WhenRoomSaveFails() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            RoomUpdateDto request = new RoomUpdateDto();
            request.setAddress("Kothrud, Pune");
            request.setRent(new BigDecimal("40000"));
            request.setDeposit(new BigDecimal("80000"));

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserAdmin(user, room))
                    .thenReturn(true);

            when(roomRepository.save(any(Room.class)))
                    .thenThrow(new RuntimeException("Database error"));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> roomService.editRoom(10L, request, userId)
            );

            assertEquals("Database error", exception.getMessage());
        }
    }


    @Nested
    class editRoomOccupancy {

        @Test
        public void editRoomOccupancy_ShouldUpdateOccupancyAndReturnDto() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            RoomOccupancyUpdateDto request =
                    new RoomOccupancyUpdateDto();

            request.setTotalOccupancy(5);

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(membershipRepository.countByRoomAndMembershipStatus(
                    eq(room),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(2L);

            when(roomRepository.save(any(Room.class)))
                    .thenReturn(room);

            RoomDetailsDto result =
                    roomService.editRoomOccupancy(
                            10L,
                            request,
                            userId
                    );

            assertEquals(10L, result.getId());
            assertEquals("Nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());

            assertEquals(5, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());

            verify(roomAccessGuide)
                    .isUserAdmin(user, room);

            verify(membershipRepository)
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );

            verify(roomRepository)
                    .save(room);
        }


        @Test
        public void editRoomOccupancy_ShouldThrowException_WhenRoomNotFound() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            RoomOccupancyUpdateDto request =
                    new RoomOccupancyUpdateDto();

            request.setTotalOccupancy(5);

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> roomService.editRoomOccupancy(
                            10L,
                            request,
                            userId
                    )
            );

            verify(roomRepository).findById(10L);

            verify(roomRepository, never())
                    .save(any(Room.class));

            verify(membershipRepository, never())
                    .countByRoomAndMembershipStatus(
                            any(Room.class),
                            any(MembershipStatus.class)
                    );
        }


        @Test
        public void editRoomOccupancy_ShouldThrowException_WhenOccupancyLessThanActiveMembers() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(5)
                    .cretedBy(user)
                    .build();

            RoomOccupancyUpdateDto request =
                    new RoomOccupancyUpdateDto();

            request.setTotalOccupancy(2);

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(membershipRepository.countByRoomAndMembershipStatus(
                    room,
                    MembershipStatus.ACTIVE
            )).thenReturn(4L);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> roomService.editRoomOccupancy(
                            10L,
                            request,
                            userId
                    )
            );

            verify(roomAccessGuide)
                    .isUserAdmin(user, room);

            verify(membershipRepository)
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );

            verify(roomRepository, never())
                    .save(any(Room.class));
        }


        @Test
        public void editRoomOccupancy_ShouldThrowException_WhenUserIsNotAdmin() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Rahul")
                    .email("rahul@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .rent(new BigDecimal("12000"))
                    .deposit(new BigDecimal("24000"))
                    .totalOccupancy(4)
                    .cretedBy(user)
                    .build();

            RoomOccupancyUpdateDto request =
                    new RoomOccupancyUpdateDto();

            request.setTotalOccupancy(5);

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            doThrow(new AccessDeniedException(
                    "Only admin can perform this action"
            ))
                    .when(roomAccessGuide)
                    .isUserAdmin(user, room);

            assertThrows(
                    AccessDeniedException.class,
                    () -> roomService.editRoomOccupancy(
                            10L,
                            request,
                            userId
                    )
            );

            verify(roomAccessGuide)
                    .isUserAdmin(user, room);

            verify(membershipRepository, never())
                    .countByRoomAndMembershipStatus(
                            any(Room.class),
                            any(MembershipStatus.class)
                    );

            verify(roomRepository, never())
                    .save(any(Room.class));
        }
    }
}