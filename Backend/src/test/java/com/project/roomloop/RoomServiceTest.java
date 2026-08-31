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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import org.springframework.security.access.AccessDeniedException;
import java.util.NoSuchElementException;
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
    private ModelMapper modelMapper;

    @InjectMocks
    private RoomService roomService;

    @Nested
    class registerNewRoom {

        //all good path
        @Test
        public void registerNewRoomIfUserNotActiveGoodPathTest(){

            // Request come from frontend
            RegisterNewRoomRequest request = new RegisterNewRoomRequest();
            request.setAddress("nana Peth");
            request.setRent(new BigDecimal("12000"));
            request.setDeposit(new BigDecimal("24000"));
            request.setTotalOccupancy(4);

            User user = User.builder()
                    .id(1L)
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

            when(membershipRepository.existsByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(false);

            when(roomRepository.save(any(Room.class)))
                    .thenReturn(savedRoom);

            when(membershipRepository.save(any(Membership.class)))
                    .thenReturn(membership);


            // Act
            RoomDetailsDto result =
                    roomService.registerNewRoom(request, user);


            // Assert
            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());
        }



        //if user have ohter active membership
        @Test
        public void registerNewRoomIfUserIsActiveInAnotherRoomTest(){

            // Request come from frontend
            RegisterNewRoomRequest request = new RegisterNewRoomRequest();
            request.setAddress("nana Peth");
            request.setRent(new BigDecimal("12000"));
            request.setDeposit(new BigDecimal("24000"));
            request.setTotalOccupancy(4);


//            Arrange data
            User user = User.builder()
                    .id(1L)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();


            //mock
            when(membershipRepository.existsByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(true);


            // calling Method
            assertThrows(
                    ActiveMembershipException.class,
                    () -> roomService.registerNewRoom(request, user)
            );


            // return Assert
            verify(roomRepository, never()).save(any(Room.class));
            verify(membershipRepository, never()).save(any(Membership.class));
        }



        // AI test cases
        @Test
        public void registerNewRoom_ShouldCreateCorrectRoom() {

            // Arrange
            RegisterNewRoomRequest request = new RegisterNewRoomRequest();

            request.setAddress("nana Peth");
            request.setRent(new BigDecimal("12000"));
            request.setDeposit(new BigDecimal("24000"));
            request.setTotalOccupancy(4);

            User user = User.builder()
                    .id(1L)
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

            // Act
            RoomDetailsDto result =
                    roomService.registerNewRoom(request, user);

            // Assert - returned DTO
            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());

            // Assert - Room was created correctly
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

            // Arrange
            RegisterNewRoomRequest request = new RegisterNewRoomRequest();

            request.setAddress("nana Peth");
            request.setRent(new BigDecimal("12000"));
            request.setDeposit(new BigDecimal("24000"));
            request.setTotalOccupancy(4);

            User user = User.builder()
                    .id(1L)
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

            // Act
            RoomDetailsDto result =
                    roomService.registerNewRoom(request, user);

            // Assert - returned DTO
            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());

            // Assert - Membership was created correctly
            verify(membershipRepository).save(argThat(membership ->
                    membership.getUser().equals(user)
                            && membership.getRoom().equals(savedRoom)
                            && membership.getIsAdmin()
                            && membership.getMembershipStatus() == MembershipStatus.ACTIVE
            ));
        }


        @Test
        public void registerNewRoom_ShouldThrowException_WhenRoomSaveFails() {

            // Arrange
            RegisterNewRoomRequest request = new RegisterNewRoomRequest();

            request.setAddress("nana Peth");
            request.setRent(new BigDecimal("12000"));
            request.setDeposit(new BigDecimal("24000"));
            request.setTotalOccupancy(4);

            User user = User.builder()
                    .id(1L)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            when(membershipRepository.existsByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(false);

            when(roomRepository.save(any(Room.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act + Assert
            assertThrows(
                    RuntimeException.class,
                    () -> roomService.registerNewRoom(request, user)
            );

            // Verify membership was never created
            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }

    @Nested
    class getRoomDetails{
        @Test // happy path test case
        public void getRoomDetails_ShouldReturnRoomDetails() {

            // Arange
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

            Membership membership = Membership.builder()
                    .user(user)
                    .room(room)
                    .isAdmin(true)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .build();

            when(membershipRepository.findByUserAndMembershipStatus(
                    eq(user),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(Optional.of(membership));


            // calling method
            RoomDetailsDto result = roomService.getRoomDetails(user);


            // Assert
            assertEquals(10L, result.getId());
            assertEquals("Nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());
        }



    @Test // ai : user has no active membership
    public void getRoomDetails_ShouldThrowException_WhenUserHasNoActiveMembership() {

        // Arrange
        User user = User.builder()
                .id(1L)
                .name("Ajinkya")
                .email("ajinkya@gmail.com")
                .build();

        when(membershipRepository.findByUserAndMembershipStatus(
                eq(user),
                eq(MembershipStatus.ACTIVE)
        )).thenReturn(Optional.empty());


        // Act + Assert
        assertThrows(
                ActiveMembershipException.class,
                () -> roomService.getRoomDetails(user)
        );
    }


    @Test //ai Verify repository was called correctly Happy Path
    public void getRoomDetails_ShouldReturnRoomDetailsVarifyRepo() {

        // Arrange
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

        Membership membership = Membership.builder()
                .user(user)
                .room(room)
                .isAdmin(true)
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();

        when(membershipRepository.findByUserAndMembershipStatus(
                eq(user),
                eq(MembershipStatus.ACTIVE)
        )).thenReturn(Optional.of(membership));


        // Act
        RoomDetailsDto result =
                roomService.getRoomDetails(user);


        // Assert
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
    class getRoomDetailsById{

        @Test // happy path test
        public void getRoomDetailsById_ShouldReturnRoomDetails() {

            // Arrange
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


            // Mocking
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(securityContext.getAuthentication())
                    .thenReturn(authentication);

            when(authentication.getPrincipal())
                    .thenReturn(user);

            SecurityContextHolder.setContext(securityContext);


            // calling methods
            RoomDetailsDto result =
                    roomService.getRoomDetailsById(10L);


            // Assert
            assertEquals(10L, result.getId());
            assertEquals("Nana Peth", result.getAddress());
            assertEquals(new BigDecimal("12000"), result.getRent());
            assertEquals(new BigDecimal("24000"), result.getDeposit());
            assertEquals(4, result.getTotalOccupancy());
            assertEquals(1L, result.getCretedByUser());


            // Cleanup
            SecurityContextHolder.clearContext();
        }

        @Test // Ai : Room doesn't exist
        public void getRoomDetailsById_ShouldThrowException_WhenRoomNotFound() {

            // Arrange
            when(roomRepository.findById(10L))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    EntityNotFoundException.class,
                    () -> roomService.getRoomDetailsById(10L)
            );

            verify(roomRepository).findById(10L);
        }


        @Test // User is not authorized this is for the idf the user is the SuperAdmin ajinkya@g,mail.com
        public void getRoomDetailsById_ShouldThrowException_WhenUserNotAuthorized() {

            // Arrange
            User user = User.builder()
                    .id(2L)
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

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));


            Authentication authentication =
                    mock(Authentication.class);

            SecurityContext securityContext =
                    mock(SecurityContext.class);

            when(securityContext.getAuthentication())
                    .thenReturn(authentication);

            when(authentication.getPrincipal())
                    .thenReturn(user);

            SecurityContextHolder.setContext(securityContext);


            // Act + Assert
            assertThrows(
                    AccessDeniedException.class,
                    () -> roomService.getRoomDetailsById(10L)
            );


            // Cleanup
            SecurityContextHolder.clearContext();
        }


        @Test // Ai : SecurityContext doesn't contain a user
        public void getRoomDetailsById_ShouldThrowException_WhenUserNotAuthenticated() {

            // Arrange
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

            SecurityContext securityContext =
                    mock(SecurityContext.class);

            when(securityContext.getAuthentication())
                    .thenReturn(null);

            SecurityContextHolder.setContext(securityContext);


            // Act + Assert
            assertThrows(
                    NullPointerException.class,
                    () -> roomService.getRoomDetailsById(10L)
            );


            // Cleanup
            SecurityContextHolder.clearContext();
        }

    }



    @Nested
    class editRoomDetails{

        @Test
        public void editRoom_ShouldUpdateRoomAndReturnDto() {

            // Arrange
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


            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomRepository.save(any(Room.class)))
                    .thenReturn(room);

            when(modelMapper.map(room, RoomDetailsDto.class))
                    .thenReturn(expectedDto);


            // Act
            RoomDetailsDto result =
                    roomService.editRoom(10L, request, user);


            // Assert
            assertEquals(10L, result.getId());
            assertEquals("Kothrud, Pune", result.getAddress());
            assertEquals(new BigDecimal("40000"), result.getRent());
            assertEquals(new BigDecimal("80000"), result.getDeposit());

            verify(roomAccessGuide)
                    .isUserAdmin(user, room);

            verify(roomRepository)
                    .save(room);
        }



        @Test // Ai : room not Found
        public void editRoom_ShouldThrowException_WhenRoomNotFound() {

            // Arrange
            User user = User.builder()
                    .id(1L)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            RoomUpdateDto request = new RoomUpdateDto();
            request.setAddress("Kothrud, Pune");
            request.setRent(new BigDecimal("40000"));
            request.setDeposit(new BigDecimal("80000"));

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.empty());


            // Act + Assert
            assertThrows(
                    EntityNotFoundException.class,
                    () -> roomService.editRoom(10L, request, user)
            );


            // Verify nothing was saved
            verify(roomRepository, never())
                    .save(any(Room.class));

            verify(roomAccessGuide, never())
                    .isUserAdmin(any(User.class), any(Room.class));
        }


        @Test // Ai : User is NOt Admin
        public void editRoom_ShouldThrowException_WhenUserIsNotAdmin() {

            // Arrange
            User user = User.builder()
                    .id(2L)
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

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            doThrow(new AccessDeniedException(
                    "Only admin can edit room"
            )).when(roomAccessGuide)
                    .isUserAdmin(user, room);


            // Act + Assert
            assertThrows(
                    AccessDeniedException.class,
                    () -> roomService.editRoom(10L, request, user)
            );


            // Verify room was NOT saved
            verify(roomRepository, never())
                    .save(any(Room.class));
        }


        @Test // Ai : room save fails
        public void editRoom_ShouldThrowException_WhenRoomSaveFails() {

            // Arrange
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

            RoomUpdateDto request = new RoomUpdateDto();
            request.setAddress("Kothrud, Pune");
            request.setRent(new BigDecimal("40000"));
            request.setDeposit(new BigDecimal("80000"));

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserAdmin(user, room))
                    .thenReturn(true);

            when(roomRepository.save(any(Room.class)))
                    .thenThrow(new RuntimeException("Database error"));


            // Act
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> roomService.editRoom(10L, request, user)
            );


            // Assert
            assertEquals("Database error", exception.getMessage());
        }


    }




    @Nested
    class editRoomOccupancy{

        @Test  // happy path test
        public void editRoomOccupancy_ShouldUpdateOccupancyAndReturnDto() {

            // Arrange
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

            RoomOccupancyUpdateDto request =
                    new RoomOccupancyUpdateDto();

            request.setTotalOccupancy(5);


            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(membershipRepository.countByRoomAndMembershipStatus(
                    eq(room),
                    eq(MembershipStatus.ACTIVE)
            )).thenReturn(2L);

            when(roomRepository.save(any(Room.class)))
                    .thenReturn(room);


            // calling method
            RoomDetailsDto result =
                    roomService.editRoomOccupancy(
                            10L,
                            request,
                            user
                    );


            // Assert
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



        @Test // ai : if Room doesn't exist
        public void editRoomOccupancy_ShouldThrowException_WhenRoomNotFound() {

            // Arrange
            User user = User.builder()
                    .id(1L)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            RoomOccupancyUpdateDto request =
                    new RoomOccupancyUpdateDto();

            request.setTotalOccupancy(5);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.empty());


            // Act + Assert
            assertThrows(
                    EntityNotFoundException.class,
                    () -> roomService.editRoomOccupancy(
                            10L,
                            request,
                            user
                    )
            );


            // Verify
            verify(roomRepository).findById(10L);

            verify(roomRepository, never())
                    .save(any(Room.class));

            verify(membershipRepository, never())
                    .countByRoomAndMembershipStatus(
                            any(Room.class),
                            any(MembershipStatus.class)
                    );
        }


        @Test // ai : New occupancy is less than current members
        public void editRoomOccupancy_ShouldThrowException_WhenOccupancyLessThanActiveMembers() {

            // Arrange
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
                    .totalOccupancy(5)
                    .cretedBy(user)
                    .build();

            RoomOccupancyUpdateDto request =
                    new RoomOccupancyUpdateDto();

            request.setTotalOccupancy(2);


            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(membershipRepository.countByRoomAndMembershipStatus(
                    room,
                    MembershipStatus.ACTIVE
            )).thenReturn(4L);


            // Act + Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> roomService.editRoomOccupancy(
                            10L,
                            request,
                            user
                    )
            );


            // Verify
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


        @Test // Ai : logi user is not admin just  a member
        public void editRoomOccupancy_ShouldThrowException_WhenUserIsNotAdmin() {

            // Arrange
            User user = User.builder()
                    .id(1L)
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


            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            doThrow(new AccessDeniedException(
                    "Only admin can perform this action"
            ))
                    .when(roomAccessGuide)
                    .isUserAdmin(user, room);


            // Act + Assert
            assertThrows(
                    AccessDeniedException.class,
                    () -> roomService.editRoomOccupancy(
                            10L,
                            request,
                            user
                    )
            );


            // Verify
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
}


