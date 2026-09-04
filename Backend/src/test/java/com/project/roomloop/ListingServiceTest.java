package com.project.roomloop;

import com.project.roomloop.dto.ListingDetailsDto;
import com.project.roomloop.dto.ListingUpdateDto;
import com.project.roomloop.dto.RegisterNewListingRequest;
import com.project.roomloop.entity.Listing;
import com.project.roomloop.entity.Membership;
import com.project.roomloop.entity.Room;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.ListingStatus;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.helperMethod.RoomAccessGuide;
import com.project.roomloop.repository.ListingRepository;
import com.project.roomloop.repository.RoomRepository;
import com.project.roomloop.service.ListingService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private RoomAccessGuide roomAccessGuide;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HelperForRoomListing helperForRoomListing;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ListingService listingService;


    @Nested
    class ListNewListing {

        // happy path it will crete listing
        @Test
        public void ListNewListing() {

            RegisterNewListingRequest request =
                    new RegisterNewListingRequest();

            request.setPreferences("Looking for clean room");
            request.setOpenSpots(2);

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
                    .build();

            Listing savedListing = Listing.builder()
                    .id(100L)
                    .room(room)
                    .postedBy(user)
                    .preferences("Looking for clean room")
                    .listingStatus(ListingStatus.OPEN)
                    .openSpots(2)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            when(listingRepository.existsByRoomAndPostedByAndListingStatus(
                    room,
                    user,
                    ListingStatus.OPEN
            )).thenReturn(false);

            when(listingRepository.save(any(Listing.class)))
                    .thenReturn(savedListing);

            when(helperForRoomListing.getCurrentRentPerMemberForListing(room))
                    .thenReturn(new BigDecimal("3000"));

            when(helperForRoomListing.getCurrentDepositPerMemberForListing(room))
                    .thenReturn(new BigDecimal("6000"));

            ListingDetailsDto result =
                    listingService.ListNewListing(request, 10L, userId);

            assertNotNull(result);
            assertEquals(100L, result.getId());
            assertEquals(10L, result.getRoom());
            assertEquals(1L, result.getPostedBy());
            assertEquals("Nana Peth", result.getAddress());
            assertEquals(2, result.getOpenSpots());
            assertEquals("Looking for clean room", result.getPreferences());
            assertEquals(ListingStatus.OPEN, result.getListingStatus());

            verify(listingRepository)
                    .save(any(Listing.class));
        }


        @Test
        public void ListNewListing_ShouldThrowException_WhenRoomNotFound() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            RegisterNewListingRequest request =
                    new RegisterNewListingRequest();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> listingService.ListNewListing(
                            request,
                            10L,
                            userId
                    )
            );

            verify(listingRepository, never())
                    .save(any(Listing.class));
        }

        // Ai
        @Test
        public void ListNewListing_ShouldThrowException_WhenUserAlreadyHasOpenListing() {

            RegisterNewListingRequest request =
                    new RegisterNewListingRequest();

            request.setPreferences("Looking for clean room");
            request.setOpenSpots(2);

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .room(room)
                    .isAdmin(true)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            when(listingRepository.existsByRoomAndPostedByAndListingStatus(
                    room,
                    user,
                    ListingStatus.OPEN
            )).thenReturn(true);

            assertThrows(
                    IllegalStateException.class,
                    () -> listingService.ListNewListing(
                            request,
                            10L,
                            userId
                    )
            );

            verify(listingRepository, never())
                    .save(any(Listing.class));
        }


        @Test
        public void ListNewListing_ShouldSetOpenSpotsOne_WhenUserIsNotAdmin() {

            RegisterNewListingRequest request =
                    new RegisterNewListingRequest();

            request.setPreferences("Need room");
            request.setOpenSpots(3);

            Long userId = 2L;

            User user = User.builder()
                    .id(userId)
                    .name("Rahul")
                    .email("rahul@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .room(room)
                    .isAdmin(false)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            when(listingRepository.existsByRoomAndPostedByAndListingStatus(
                    room,
                    user,
                    ListingStatus.OPEN
            )).thenReturn(false);

            when(listingRepository.save(any(Listing.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(helperForRoomListing.getCurrentRentPerMemberForListing(room))
                    .thenReturn(new BigDecimal("3000"));

            when(helperForRoomListing.getCurrentDepositPerMemberForListing(room))
                    .thenReturn(new BigDecimal("6000"));

            listingService.ListNewListing(
                    request,
                    10L,
                    userId
            );

            verify(listingRepository).save(argThat(listing ->
                    listing.getOpenSpots() == 1
                            && listing.getPostedBy().equals(user)
                            && listing.getRoom().equals(room)
                            && listing.getListingStatus() == ListingStatus.OPEN
            ));
        }


        @Test
        public void ListNewListing_ShouldThrowException_WhenUserNotFound() {

            Long userId = 1L;

            RegisterNewListingRequest request =
                    new RegisterNewListingRequest();

            when(helperForRoomListing.checkUser(userId))
                    .thenThrow(new EntityNotFoundException(
                            "User not found with this id"
                    ));

            assertThrows(
                    EntityNotFoundException.class,
                    () -> listingService.ListNewListing(
                            request,
                            10L,
                            userId
                    )
            );

            verify(roomRepository, never())
                    .findById(anyLong());

            verify(listingRepository, never())
                    .save(any(Listing.class));
        }
    }


    @Nested
    class getMyListing {

        // happy path // return memeber psoted listing
        @Test
        public void getMyListing() {

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
                    .build();

            Listing listing = Listing.builder()
                    .id(100L)
                    .room(room)
                    .postedBy(user)
                    .preferences("Need clean room")
                    .listingStatus(ListingStatus.OPEN)
                    .openSpots(1)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            when(listingRepository.findByPostedBy(user))
                    .thenReturn(Optional.of(listing));

            when(helperForRoomListing.getCurrentRentPerMemberForListing(room))
                    .thenReturn(new BigDecimal("3000"));

            when(helperForRoomListing.getCurrentDepositPerMemberForListing(room))
                    .thenReturn(new BigDecimal("6000"));

            ListingDetailsDto result =
                    listingService.getMyListing(10L, userId);

            assertNotNull(result);
            assertEquals(100L, result.getId());
            assertEquals(10L, result.getRoom());
            assertEquals(1L, result.getPostedBy());
            assertEquals("Need clean room", result.getPreferences());
        }



        // Ai
        @Test
        public void getMyListing_ShouldThrowException_WhenRoomNotFound() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> listingService.getMyListing(10L, userId)
            );

            verify(listingRepository, never())
                    .findByPostedBy(any(User.class));
        }


        @Test
        public void getMyListing_ShouldThrowException_WhenListingNotFound() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .room(room)
                    .isAdmin(true)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            when(listingRepository.findByPostedBy(user))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> listingService.getMyListing(10L, userId)
            );
        }
    }


    @Nested
    class deleteMyListing {

        // happy path delete member listing by self
        @Test
        public void deleteMyListing() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .room(room)
                    .isAdmin(true)
                    .build();

            Listing listing = Listing.builder()
                    .id(100L)
                    .room(room)
                    .postedBy(user)
                    .preferences("Need room")
                    .listingStatus(ListingStatus.OPEN)
                    .openSpots(1)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            when(listingRepository.findByPostedBy(user))
                    .thenReturn(Optional.of(listing));

            when(listingRepository.save(any(Listing.class)))
                    .thenReturn(listing);

            ListingDetailsDto result =
                    listingService.deleteMyListing(10L, userId);

            assertNotNull(result);
            assertEquals(ListingStatus.CLOSED, listing.getListingStatus());

            verify(listingRepository)
                    .save(listing);
        }


        @Test
        public void deleteMyListing_ShouldThrowException_WhenListingNotFound() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .room(room)
                    .isAdmin(true)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            when(listingRepository.findByPostedBy(user))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> listingService.deleteMyListing(10L, userId)
            );

            verify(listingRepository, never())
                    .save(any(Listing.class));
        }
    }


    @Nested
    class editMyListing {

        // happy path edit listing by member
        @Test
        public void editMyListing() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .email("ajinkya@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .address("Nana Peth")
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .room(room)
                    .isAdmin(true)
                    .build();

            Listing listing = Listing.builder()
                    .id(100L)
                    .room(room)
                    .postedBy(user)
                    .preferences("Old preference")
                    .listingStatus(ListingStatus.OPEN)
                    .openSpots(1)
                    .build();

            ListingUpdateDto request =
                    new ListingUpdateDto();

            request.setPreferences("New preference");

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            when(listingRepository.findByPostedBy(user))
                    .thenReturn(Optional.of(listing));

            when(listingRepository.save(any(Listing.class)))
                    .thenReturn(listing);

            ListingDetailsDto result =
                    listingService.editMyListing(
                            request,
                            10L,
                            userId
                    );

            assertNotNull(result);
            assertEquals("New preference", listing.getPreferences());

            verify(listingRepository)
                    .save(listing);
        }

        // Ai
        @Test
        public void editMyListing_ShouldThrowException_WhenRoomNotFound() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            ListingUpdateDto request =
                    new ListingUpdateDto();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> listingService.editMyListing(
                            request,
                            10L,
                            userId
                    )
            );

            verify(listingRepository, never())
                    .findByPostedBy(any(User.class));
        }


        @Test
        public void editMyListing_ShouldThrowException_WhenListingNotFound() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .build();

            Membership membership = Membership.builder()
                    .user(user)
                    .room(room)
                    .isAdmin(true)
                    .build();

            ListingUpdateDto request =
                    new ListingUpdateDto();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(10L))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuide.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            when(listingRepository.findByPostedBy(user))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> listingService.editMyListing(
                            request,
                            10L,
                            userId
                    )
            );
        }
    }


    @Nested
    class getAllListings {

        @Test
        public void getAllListings_ShouldReturnAllOpenListings() {

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

            Listing listing = Listing.builder()
                    .id(100L)
                    .room(room)
                    .postedBy(user)
                    .preferences("Need room")
                    .listingStatus(ListingStatus.OPEN)
                    .openSpots(1)
                    .build();

            Page<Listing> page = new PageImpl<>(List.of(listing));

            when(listingRepository.findByListingStatus(
                    eq(ListingStatus.OPEN),
                    any(PageRequest.class)
            )).thenReturn(page);





            when(helperForRoomListing.getCurrentRentPerMemberForListing(room))
                    .thenReturn(new BigDecimal("3000"));

            when(helperForRoomListing.getCurrentDepositPerMemberForListing(room))
                    .thenReturn(new BigDecimal("6000"));

            List<ListingDetailsDto> result =
                    listingService.getAllListings(0, 10);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(100L, result.get(0).getId());
            assertEquals(10L, result.get(0).getRoom());
            assertEquals(1L, result.get(0).getPostedBy());
            assertEquals(ListingStatus.OPEN, result.get(0).getListingStatus());

            verify(listingRepository)
                    .findByListingStatus(
                            eq(ListingStatus.OPEN),
                            any(PageRequest.class)
                    );
        }


        @Test
        public void getAllListings_ShouldReturnEmptyList_WhenNoListingsFound() {

            Page<Listing> page = new PageImpl<>(List.of());

            when(listingRepository.findByListingStatus(
                    eq(ListingStatus.OPEN),
                    any(PageRequest.class)
            )).thenReturn(page);

            List<ListingDetailsDto> result =
                    listingService.getAllListings(0, 10);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }


    @Nested
    class getListingById {

        @Test
        public void getListingById_ShouldReturnListing() {

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

            Listing listing = Listing.builder()
                    .id(100L)
                    .room(room)
                    .postedBy(user)
                    .preferences("Need room")
                    .listingStatus(ListingStatus.OPEN)
                    .openSpots(1)
                    .build();

            when(listingRepository.findById(100L))
                    .thenReturn(Optional.of(listing));

            when(helperForRoomListing.getCurrentRentPerMemberForListing(room))
                    .thenReturn(new BigDecimal("3000"));

            when(helperForRoomListing.getCurrentDepositPerMemberForListing(room))
                    .thenReturn(new BigDecimal("6000"));

            ListingDetailsDto result =
                    listingService.getListingById(100L);

            assertNotNull(result);
            assertEquals(100L, result.getId());
            assertEquals(10L, result.getRoom());
            assertEquals(1L, result.getPostedBy());
            assertEquals("Nana Peth", result.getAddress());
            assertEquals("Need room", result.getPreferences());
            assertEquals(ListingStatus.OPEN, result.getListingStatus());
        }


        @Test
        public void getListingById_ShouldThrowException_WhenListingNotFound() {

            when(listingRepository.findById(100L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> listingService.getListingById(100L)
            );

            verify(listingRepository)
                    .findById(100L);
        }
    }
}