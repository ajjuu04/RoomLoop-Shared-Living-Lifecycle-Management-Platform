package com.project.roomloop;

import com.project.roomloop.entity.Listing;
import com.project.roomloop.entity.Membership;
import com.project.roomloop.entity.Room;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.ListingStatus;
import com.project.roomloop.entity.types.MembershipStatus;
import com.project.roomloop.error.ResourceNotFoundException;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.helperMethod.RoomAccessGuide;
import com.project.roomloop.repository.ListingRepository;
import com.project.roomloop.repository.MembershipRepository;
import com.project.roomloop.repository.RoomRepository;
import com.project.roomloop.repository.UserRepository;
import com.project.roomloop.service.MembershipService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MembershipServiceTest {

    @InjectMocks
    private MembershipService membershipService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private RoomAccessGuide roomAccessGuard;

    @Mock
    private HelperForRoomListing helperForRoomListing;

    @Mock
    private ListingRepository listingRepository;


    // =========================================================
    // createJoinRequest()
    // =========================================================

    @Nested
    class CreateJoinRequestTests {

        @Test
        void happyPath() {

            Long userId = 1L;
            Long listingId = 101L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .totalOccupancy(4)
                    .build();

            Listing listing = Listing.builder()
                    .id(listingId)
                    .listingStatus(ListingStatus.OPEN)
                    .room(room)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(listingRepository.findById(listingId))
                    .thenReturn(Optional.of(listing));

            membershipService.createJoinRequest(userId, listingId);

            verify(helperForRoomListing).checkUser(userId);
            verify(listingRepository).findById(listingId);
            verify(membershipRepository).save(any(Membership.class));
        }


        @Test
        void listingNotFound() {

            Long userId = 1L;
            Long listingId = 101L;

            User user = User.builder()
                    .id(userId)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(listingRepository.findById(listingId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.createJoinRequest(userId, listingId)
            );

            verify(helperForRoomListing).checkUser(userId);
            verify(listingRepository).findById(listingId);

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }


        @Test
        void listingIsClosed() {

            Long userId = 1L;
            Long listingId = 101L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .build();

            Listing listing = Listing.builder()
                    .id(listingId)
                    .listingStatus(ListingStatus.CLOSED)
                    .room(room)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(listingRepository.findById(listingId))
                    .thenReturn(Optional.of(listing));

            assertThrows(
                    IllegalStateException.class,
                    () -> membershipService.createJoinRequest(userId, listingId)
            );

            verify(helperForRoomListing).checkUser(userId);
            verify(listingRepository).findById(listingId);

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }
    }


    // =========================================================
    // createJoinRequestByRoomId()
    // =========================================================

    @Nested
    class CreateJoinRequestByRoomIdTests {

        @Test
        void happyPath() {

            Long userId = 1L;
            Long roomId = 10L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya J")
                    .email("aj@gmail.com")
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .totalOccupancy(4)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(membershipRepository
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(3L);

            membershipService.createJoinRequestByRoomId(userId, roomId);

            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(roomRepository)
                    .findById(roomId);

            verify(membershipRepository)
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );

            verify(membershipRepository)
                    .save(any(Membership.class));
        }


        @Test
        void roomNotFound() {

            Long userId = 1L;
            Long roomId = 10L;

            User user = User.builder()
                    .id(userId)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.createJoinRequestByRoomId(userId, roomId)
            );

            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(roomRepository)
                    .findById(roomId);

            verify(membershipRepository, never())
                    .countByRoomAndMembershipStatus(
                            any(Room.class),
                            eq(MembershipStatus.ACTIVE)
                    );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }


        @Test
        void roomIsFull() {

            Long userId = 1L;
            Long roomId = 10L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .totalOccupancy(4)
                    .build();

            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(membershipRepository
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(4L);

            assertThrows(
                    IllegalStateException.class,
                    () -> membershipService.createJoinRequestByRoomId(userId, roomId)
            );

            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(roomRepository)
                    .findById(roomId);

            verify(membershipRepository)
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }
    }


    // =========================================================
    // getRoomMembers()
    // =========================================================

    @Nested
    class GetRoomMembersTests {

        @Test
        void happyPath() {

            Long userId = 1L;
            Long roomId = 10L;

            User user = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .build();

            User adminUser = User.builder()
                    .id(2L)
                    .name("Admin")
                    .build();

            User memberUser = User.builder()
                    .id(3L)
                    .name("Member")
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            Membership adminMembership = Membership.builder()
                    .id(101L)
                    .user(adminUser)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .isAdmin(true)
                    .build();

            Membership normalMembership = Membership.builder()
                    .id(102L)
                    .user(memberUser)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .isAdmin(false)
                    .build();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(membershipRepository.findByRoomAndMembershipStatus(
                    room,
                    MembershipStatus.ACTIVE
            )).thenReturn(List.of(
                    adminMembership,
                    normalMembership
            ));

            membershipService.getRoomMembers(roomId, userId);

            verify(userRepository).findById(userId);
            verify(roomRepository).findById(roomId);
            verify(roomAccessGuard)
                    .isUserActiveMemberOfRoom(user, room);

            verify(membershipRepository)
                    .findByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );
        }


        @Test
        void userNotFound() {

            Long userId = 1L;
            Long roomId = 10L;

            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.getRoomMembers(roomId, userId)
            );

            verify(userRepository).findById(userId);

            verify(roomRepository, never())
                    .findById(roomId);

            verify(membershipRepository, never())
                    .findByRoomAndMembershipStatus(
                            any(Room.class),
                            any(MembershipStatus.class)
                    );
        }


        @Test
        void roomNotFound() {

            Long userId = 1L;
            Long roomId = 10L;

            User user = User.builder()
                    .id(userId)
                    .build();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.getRoomMembers(roomId, userId)
            );

            verify(userRepository).findById(userId);
            verify(roomRepository).findById(roomId);

            verify(roomAccessGuard, never())
                    .isUserActiveMemberOfRoom(
                            any(User.class),
                            any(Room.class)
                    );
        }
    }


    // =========================================================
    // getJoinRequests()
    // =========================================================

    @Nested
    class GetJoinRequestsTests {

        @Test
        void happyPath() {

            Long userId = 1L;
            Long roomId = 10L;

            User admin = User.builder()
                    .id(userId)
                    .name("Ajinkya")
                    .build();

            User requester = User.builder()
                    .id(2L)
                    .name("Rahul")
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            Membership request = Membership.builder()
                    .id(100L)
                    .user(requester)
                    .room(room)
                    .membershipStatus(MembershipStatus.PENDING)
                    .isAdmin(false)
                    .build();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(admin));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(membershipRepository.findByRoomAndMembershipStatus(
                    room,
                    MembershipStatus.PENDING
            )).thenReturn(List.of(request));

            membershipService.getJoinRequests(roomId, userId);

            verify(userRepository).findById(userId);
            verify(roomRepository).findById(roomId);
            verify(roomAccessGuard).isUserAdmin(admin, room);
            verify(membershipRepository)
                    .findByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.PENDING
                    );
        }


        @Test
        void userNotFound() {

            Long userId = 1L;
            Long roomId = 10L;

            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.getJoinRequests(roomId, userId)
            );

            verify(userRepository).findById(userId);

            verify(roomRepository, never())
                    .findById(roomId);
        }


        @Test
        void roomNotFound() {

            Long userId = 1L;
            Long roomId = 10L;

            User user = User.builder()
                    .id(userId)
                    .build();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.getJoinRequests(roomId, userId)
            );

            verify(userRepository).findById(userId);
            verify(roomRepository).findById(roomId);

            verify(roomAccessGuard, never())
                    .isUserAdmin(any(User.class), any(Room.class));
        }
    }


    // =========================================================
    // approveJoinRequest()
    // =========================================================

    @Nested
    class ApproveJoinRequestTests {

        @Test
        void happyPath() {

            Long membershipId = 100L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .name("Admin")
                    .build();

            User requester = User.builder()
                    .id(2L)
                    .name("Requester")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .totalOccupancy(4)
                    .build();

            Membership membership = Membership.builder()
                    .id(membershipId)
                    .user(requester)
                    .room(room)
                    .membershipStatus(MembershipStatus.PENDING)
                    .isAdmin(false)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(membershipRepository.findById(membershipId))
                    .thenReturn(Optional.of(membership));

            when(membershipRepository
                    .existsByUser_IdAndMembershipStatus(
                            requester.getId(),
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(false);

            when(membershipRepository
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(2L);

            membershipService.approveJoinRequest(
                    membershipId,
                    adminUserId
            );

            assertEquals(
                    MembershipStatus.ACTIVE,
                    membership.getMembershipStatus()
            );

            verify(roomAccessGuard)
                    .isUserAdmin(admin, room);

            verify(membershipRepository)
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );

            verify(membershipRepository)
                    .save(membership);
        }


        @Test
        void membershipNotFound() {

            Long membershipId = 100L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(membershipRepository.findById(membershipId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.approveJoinRequest(
                            membershipId,
                            adminUserId
                    )
            );

            verify(membershipRepository)
                    .findById(membershipId);

            verify(roomAccessGuard, never())
                    .isUserAdmin(any(User.class), any(Room.class));

            verify(membershipRepository, never())
                    .existsByUser_IdAndMembershipStatus(
                            anyLong(),
                            any(MembershipStatus.class)
                    );

            verify(membershipRepository, never())
                    .countByRoomAndMembershipStatus(
                            any(Room.class),
                            any(MembershipStatus.class)
                    );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }


        @Test
        void requestIsNotPending() {

            Long membershipId = 100L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            User requester = User.builder()
                    .id(2L)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .totalOccupancy(4)
                    .build();

            Membership membership = Membership.builder()
                    .id(membershipId)
                    .user(requester)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(membershipRepository.findById(membershipId))
                    .thenReturn(Optional.of(membership));

            assertThrows(
                    IllegalStateException.class,
                    () -> membershipService.approveJoinRequest(
                            membershipId,
                            adminUserId
                    )
            );

            verify(roomAccessGuard, never())
                    .isUserAdmin(any(User.class), any(Room.class));

            verify(membershipRepository, never())
                    .existsByUser_IdAndMembershipStatus(
                            anyLong(),
                            any(MembershipStatus.class)
                    );

            verify(membershipRepository, never())
                    .countByRoomAndMembershipStatus(
                            any(Room.class),
                            any(MembershipStatus.class)
                    );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }


        @Test
        void requesterAlreadyActive() {

            Long membershipId = 100L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            User requester = User.builder()
                    .id(2L)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .totalOccupancy(4)
                    .build();

            Membership membership = Membership.builder()
                    .id(membershipId)
                    .user(requester)
                    .room(room)
                    .membershipStatus(MembershipStatus.PENDING)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(membershipRepository.findById(membershipId))
                    .thenReturn(Optional.of(membership));

            when(membershipRepository
                    .existsByUser_IdAndMembershipStatus(
                            requester.getId(),
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(true);

            assertThrows(
                    IllegalStateException.class,
                    () -> membershipService.approveJoinRequest(
                            membershipId,
                            adminUserId
                    )
            );

            verify(roomAccessGuard)
                    .isUserAdmin(admin, room);

            verify(membershipRepository)
                    .existsByUser_IdAndMembershipStatus(
                            requester.getId(),
                            MembershipStatus.ACTIVE
                    );

            // Capacity check should NOT happen
            verify(membershipRepository, never())
                    .countByRoomAndMembershipStatus(
                            any(Room.class),
                            any(MembershipStatus.class)
                    );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }


        @Test
        void roomIsAtFullCapacity() {

            Long membershipId = 100L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .name("Admin")
                    .build();

            User requester = User.builder()
                    .id(2L)
                    .name("Requester")
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .totalOccupancy(4)
                    .build();

            Membership membership = Membership.builder()
                    .id(membershipId)
                    .user(requester)
                    .room(room)
                    .membershipStatus(MembershipStatus.PENDING)
                    .isAdmin(false)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(membershipRepository.findById(membershipId))
                    .thenReturn(Optional.of(membership));

            when(membershipRepository
                    .existsByUser_IdAndMembershipStatus(
                            requester.getId(),
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(false);

            // Room capacity = 4
            // Current active members = 4
            when(membershipRepository
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(4L);

            assertThrows(
                    IllegalStateException.class,
                    () -> membershipService.approveJoinRequest(
                            membershipId,
                            adminUserId
                    )
            );

            verify(roomAccessGuard)
                    .isUserAdmin(admin, room);

            verify(membershipRepository)
                    .existsByUser_IdAndMembershipStatus(
                            requester.getId(),
                            MembershipStatus.ACTIVE
                    );

            verify(membershipRepository)
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );

            // Membership must remain PENDING
            assertEquals(
                    MembershipStatus.PENDING,
                    membership.getMembershipStatus()
            );

            // Save must NOT happen
            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }


        @Test
        void roomHasAvailableCapacity() {

            Long membershipId = 100L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            User requester = User.builder()
                    .id(2L)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .totalOccupancy(4)
                    .build();

            Membership membership = Membership.builder()
                    .id(membershipId)
                    .user(requester)
                    .room(room)
                    .membershipStatus(MembershipStatus.PENDING)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(membershipRepository.findById(membershipId))
                    .thenReturn(Optional.of(membership));

            when(membershipRepository
                    .existsByUser_IdAndMembershipStatus(
                            requester.getId(),
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(false);

            // Capacity = 4
            // Current active members = 3
            when(membershipRepository
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(3L);

            membershipService.approveJoinRequest(
                    membershipId,
                    adminUserId
            );

            assertEquals(
                    MembershipStatus.ACTIVE,
                    membership.getMembershipStatus()
            );

            verify(roomAccessGuard)
                    .isUserAdmin(admin, room);

            verify(membershipRepository)
                    .existsByUser_IdAndMembershipStatus(
                            requester.getId(),
                            MembershipStatus.ACTIVE
                    );

            verify(membershipRepository)
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );

            verify(membershipRepository)
                    .save(membership);
        }


        @Test
        void roomCapacityExceeded() {

            Long membershipId = 100L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            User requester = User.builder()
                    .id(2L)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .totalOccupancy(4)
                    .build();

            Membership membership = Membership.builder()
                    .id(membershipId)
                    .user(requester)
                    .room(room)
                    .membershipStatus(MembershipStatus.PENDING)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(membershipRepository.findById(membershipId))
                    .thenReturn(Optional.of(membership));

            when(membershipRepository
                    .existsByUser_IdAndMembershipStatus(
                            requester.getId(),
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(false);

            // Current members > capacity
            when(membershipRepository
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    ))
                    .thenReturn(5L);

            assertThrows(
                    IllegalStateException.class,
                    () -> membershipService.approveJoinRequest(
                            membershipId,
                            adminUserId
                    )
            );

            assertEquals(
                    MembershipStatus.PENDING,
                    membership.getMembershipStatus()
            );

            verify(roomAccessGuard)
                    .isUserAdmin(admin, room);

            verify(membershipRepository)
                    .countByRoomAndMembershipStatus(
                            room,
                            MembershipStatus.ACTIVE
                    );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }
    }
    // =========================================================
    // rejectJoinRequest()
    // =========================================================

    @Nested
    class RejectJoinRequestTests {

        @Test
        void happyPath() {

            Long membershipId = 100L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            User requester = User.builder()
                    .id(2L)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .build();

            Membership membership = Membership.builder()
                    .id(membershipId)
                    .user(requester)
                    .room(room)
                    .membershipStatus(MembershipStatus.PENDING)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(membershipRepository.findById(membershipId))
                    .thenReturn(Optional.of(membership));

            membershipService.rejectJoinRequest(
                    membershipId,
                    adminUserId
            );

            verify(roomAccessGuard)
                    .isUserAdmin(admin, room);

            verify(membershipRepository)
                    .delete(membership);
        }


        @Test
        void membershipNotFound() {

            Long membershipId = 100L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(membershipRepository.findById(membershipId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.rejectJoinRequest(
                            membershipId,
                            adminUserId
                    )
            );

            verify(membershipRepository, never())
                    .delete(any(Membership.class));
        }


        @Test
        void requestIsNotPending() {

            Long membershipId = 100L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            Room room = Room.builder()
                    .id(10L)
                    .build();

            Membership membership = Membership.builder()
                    .id(membershipId)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(membershipRepository.findById(membershipId))
                    .thenReturn(Optional.of(membership));

            assertThrows(
                    IllegalStateException.class,
                    () -> membershipService.rejectJoinRequest(
                            membershipId,
                            adminUserId
                    )
            );

            verify(roomAccessGuard, never())
                    .isUserAdmin(any(User.class), any(Room.class));

            verify(membershipRepository, never())
                    .delete(any(Membership.class));
        }
    }


    // =========================================================
    // exitRoom()
    // =========================================================

    @Nested
    class ExitRoomTests {

        @Test
        void happyPath() {

            Long roomId = 10L;
            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            Membership membership = Membership.builder()
                    .id(100L)
                    .user(user)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .isAdmin(false)
                    .build();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuard.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            membershipService.exitRoom(roomId, userId);

            assertEquals(
                    MembershipStatus.LEFT,
                    membership.getMembershipStatus()
            );

            verify(membershipRepository)
                    .save(membership);
        }


        @Test
        void userIsAdmin() {

            Long roomId = 10L;
            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            Membership membership = Membership.builder()
                    .id(100L)
                    .user(user)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .isAdmin(true)
                    .build();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuard.isUserActiveMemberOfRoom(user, room))
                    .thenReturn(membership);

            assertThrows(
                    IllegalStateException.class,
                    () -> membershipService.exitRoom(roomId, userId)
            );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }


        @Test
        void userNotFound() {

            Long roomId = 10L;
            Long userId = 1L;

            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.exitRoom(roomId, userId)
            );

            verify(roomRepository, never())
                    .findById(roomId);
        }


        @Test
        void roomNotFound() {

            Long roomId = 10L;
            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.exitRoom(roomId, userId)
            );

            verify(roomAccessGuard, never())
                    .isUserActiveMemberOfRoom(
                            any(User.class),
                            any(Room.class)
                    );
        }
    }


    // =========================================================
    // removeMember()
    // =========================================================

    @Nested
    class RemoveMemberTests {

        @Test
        void happyPath() {

            Long roomId = 10L;
            Long removeUserId = 2L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            User member = User.builder()
                    .id(removeUserId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            Membership membership = Membership.builder()
                    .id(100L)
                    .user(member)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .isAdmin(false)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(membershipRepository
                    .findByUser_IdAndRoom_IdAndMembershipStatus(
                            removeUserId,
                            roomId,
                            MembershipStatus.ACTIVE
                    )).thenReturn(Optional.of(membership));

            membershipService.removeMember(
                    roomId,
                    removeUserId,
                    adminUserId
            );

            verify(roomAccessGuard)
                    .isUserAdmin(admin, room);

            assertEquals(
                    MembershipStatus.LEFT,
                    membership.getMembershipStatus()
            );

            verify(membershipRepository)
                    .save(membership);
        }


        @Test
        void adminCannotRemoveSelf() {

            Long roomId = 10L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> membershipService.removeMember(
                            roomId,
                            adminUserId,
                            adminUserId
                    )
            );

            verify(roomAccessGuard)
                    .isUserAdmin(admin, room);

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }


        @Test
        void memberNotFound() {

            Long roomId = 10L;
            Long removeUserId = 2L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(membershipRepository
                    .findByUser_IdAndRoom_IdAndMembershipStatus(
                            removeUserId,
                            roomId,
                            MembershipStatus.ACTIVE
                    )).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> membershipService.removeMember(
                            roomId,
                            removeUserId,
                            adminUserId
                    )
            );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }
    }


    // =========================================================
    // transferAdminPosition()
    // =========================================================

    @Nested
    class TransferAdminPositionTests {

        @Test
        void happyPath() {

            Long roomId = 10L;
            Long newAdminUserId = 2L;
            Long currentAdminUserId = 1L;

            User currentAdmin = User.builder()
                    .id(currentAdminUserId)
                    .build();

            User newAdmin = User.builder()
                    .id(newAdminUserId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            Membership currentAdminMembership = Membership.builder()
                    .id(100L)
                    .user(currentAdmin)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .isAdmin(true)
                    .build();

            Membership newAdminMembership = Membership.builder()
                    .id(101L)
                    .user(newAdmin)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .isAdmin(false)
                    .build();

            when(userRepository.findById(currentAdminUserId))
                    .thenReturn(Optional.of(currentAdmin));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuard.requireAdmin(currentAdmin, room))
                    .thenReturn(currentAdminMembership);

            when(membershipRepository
                    .findByUser_IdAndRoom_IdAndMembershipStatus(
                            newAdminUserId,
                            roomId,
                            MembershipStatus.ACTIVE
                    )).thenReturn(Optional.of(newAdminMembership));

            membershipService.transferAdminPosition(
                    roomId,
                    newAdminUserId,
                    currentAdminUserId
            );

            assertFalse(currentAdminMembership.getIsAdmin());
            assertTrue(newAdminMembership.getIsAdmin());

            verify(membershipRepository)
                    .save(currentAdminMembership);

            verify(membershipRepository)
                    .save(newAdminMembership);
        }


        @Test
        void currentAdminIsSameAsNewAdmin() {

            Long roomId = 10L;
            Long adminUserId = 1L;

            User admin = User.builder()
                    .id(adminUserId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            Membership membership = Membership.builder()
                    .id(100L)
                    .user(admin)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .isAdmin(true)
                    .build();

            when(userRepository.findById(adminUserId))
                    .thenReturn(Optional.of(admin));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuard.requireAdmin(admin, room))
                    .thenReturn(membership);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> membershipService.transferAdminPosition(
                            roomId,
                            adminUserId,
                            adminUserId
                    )
            );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }


        @Test
        void newAdminIsNotActiveMember() {

            Long roomId = 10L;
            Long newAdminUserId = 2L;
            Long currentAdminUserId = 1L;

            User currentAdmin = User.builder()
                    .id(currentAdminUserId)
                    .build();

            Room room = Room.builder()
                    .id(roomId)
                    .build();

            Membership currentAdminMembership = Membership.builder()
                    .id(100L)
                    .user(currentAdmin)
                    .room(room)
                    .membershipStatus(MembershipStatus.ACTIVE)
                    .isAdmin(true)
                    .build();

            when(userRepository.findById(currentAdminUserId))
                    .thenReturn(Optional.of(currentAdmin));

            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            when(roomAccessGuard.requireAdmin(currentAdmin, room))
                    .thenReturn(currentAdminMembership);

            when(membershipRepository
                    .findByUser_IdAndRoom_IdAndMembershipStatus(
                            newAdminUserId,
                            roomId,
                            MembershipStatus.ACTIVE
                    )).thenReturn(Optional.empty());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> membershipService.transferAdminPosition(
                            roomId,
                            newAdminUserId,
                            currentAdminUserId
                    )
            );

            verify(membershipRepository, never())
                    .save(any(Membership.class));
        }
    }
}