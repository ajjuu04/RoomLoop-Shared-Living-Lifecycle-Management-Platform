package com.project.roomloop.service;

import com.project.roomloop.dto.*;
import com.project.roomloop.entity.*;
import com.project.roomloop.entity.types.ListingStatus;
import com.project.roomloop.entity.types.MembershipStatus;
import com.project.roomloop.error.ResourceNotFoundException;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.helperMethod.RoomAccessGuide;
import com.project.roomloop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final MembershipRepository membershipRepository;
    private final RoomAccessGuide roomAccessGuard;
    private final HelperForRoomListing helperForRoomListing;
    private final ListingRepository listingRepository;

    // this for the person who want to join with listting
    @Transactional
    public void createJoinRequest(Long userId, Long listingId) {
        log.info("Entering to the service");
        User user = helperForRoomListing.checkUser(userId);
        log.info("user login : "+ user.getName());

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing with this id not found : " + listingId));

        log.info("listing check  : ");

        if (listing.getListingStatus() != ListingStatus.OPEN) {
            throw new IllegalStateException("This listing is closed or already filled");
        }

        createPendingMembership(user, listing.getRoom());
    }

    // this for the person who want to join with room id
    @Transactional
    public void createJoinRequestByRoomId(Long userId, Long roomId) {
        User user = helperForRoomListing.checkUser(userId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        long currentActiveMembers = membershipRepository.countByRoomAndMembershipStatus(room, MembershipStatus.ACTIVE);
        if (currentActiveMembers >= room.getTotalOccupancy()) {
            throw new IllegalStateException("This room has no open spots right now");
        }

        createPendingMembership(user, room);
    }

    private void createPendingMembership(User user, Room room) {
        boolean alreadyActiveElsewhere = membershipRepository
                .existsByUserAndMembershipStatus(user, MembershipStatus.ACTIVE);
        log.info("checking the user is acctive or not other ");
        if (alreadyActiveElsewhere) {
            throw new IllegalStateException("You must leave your current room before joining another room");
        }

        log.info("checking already Active Else where ");
        boolean alreadyRequested = membershipRepository
                .existsByUserAndRoomAndMembershipStatus(user, room, MembershipStatus.PENDING);
        if (alreadyRequested) {
            throw new IllegalStateException("You already have a pending join request with this room");
        }

        log.info("checking already Requested");

        membershipRepository.save(Membership.builder()
                .user(user).room(room)
                .membershipStatus(MembershipStatus.PENDING)
                .isAdmin(false)
                .build());
    }

    public RoomMembersDto getRoomMembers(Long roomId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        roomAccessGuard.isUserActiveMemberOfRoom(user, room); // room memebe rshould be the active rm

        List<Membership> active = membershipRepository.findByRoomAndMembershipStatus(room, MembershipStatus.ACTIVE);

        MemberDto admin = active.stream()
                .filter(Membership::getIsAdmin)
                .map(m -> new MemberDto(m.getUser().getId(), m.getUser().getName(), true))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Room has no admin there is some issue data is is not custante"));

        List<MemberDto> members = active.stream()
                .filter(m -> !m.getIsAdmin())
                .map(m -> new MemberDto(m.getUser().getId(), m.getUser().getName(), false))
                .toList();

        return new RoomMembersDto(admin, members);
    }

    public List<JoinRequestsDto> getJoinRequests(Long roomId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        roomAccessGuard.isUserAdmin(user, room);

        return membershipRepository.findByRoomAndMembershipStatus(room, MembershipStatus.PENDING).stream()
                .map(m -> new JoinRequestsDto(m.getId(), m.getUser().getId(), m.getUser().getName()))
                .toList();
    }

    @Transactional
    public void approveJoinRequest(Long membershipId, Long adminUserId) {
        User admin = userRepository.findById(adminUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));

        if (membership.getMembershipStatus() != MembershipStatus.PENDING) {
            throw new IllegalStateException("This request is not pending");
        }
        roomAccessGuard.isUserAdmin(admin, membership.getRoom());

        boolean requesterAlreadyActive = membershipRepository
                .existsByUser_IdAndMembershipStatus(membership.getUser().getId(), MembershipStatus.ACTIVE);
        if (requesterAlreadyActive) {
            throw new IllegalStateException("This user already has an active room elsewhere");
        }

        membership.setMembershipStatus(MembershipStatus.ACTIVE);
        membershipRepository.save(membership);
    }

    @Transactional
    public void rejectJoinRequest(Long membershipId, Long adminUserId) {
        User admin = userRepository.findById(adminUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));

        if (membership.getMembershipStatus() != MembershipStatus.PENDING) {
            throw new IllegalStateException("This request is not pending");
        }
        roomAccessGuard.isUserAdmin(admin, membership.getRoom());

        membershipRepository.delete(membership);
    }

    @Transactional
    public void exitRoom(Long roomId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        Membership membership = roomAccessGuard.isUserActiveMemberOfRoom(user, room);

        if (membership.getIsAdmin()) {
            throw new IllegalStateException("you are the room admin first mk eanother member admin before leaving");
        }

        membership.setMembershipStatus(MembershipStatus.LEFT);
        membershipRepository.save(membership);
    }

    @Transactional
    public void removeMember(Long roomId, Long remooveUser_UserId, Long adminUserId) {
        User admin = userRepository.findById(adminUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        roomAccessGuard.isUserAdmin(admin, room);

        if (remooveUser_UserId.equals(adminUserId)) {
            throw new IllegalArgumentException("you are admin you cant remove self first make someone else admin before leaving");
        }

        Membership RemovedMember = membershipRepository
                .findByUser_IdAndRoom_IdAndMembershipStatus(remooveUser_UserId, roomId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("This user is not an active member of this room"));

        RemovedMember.setMembershipStatus(MembershipStatus.LEFT);
        membershipRepository.save(RemovedMember);
    }

    @Transactional
    public void transferAdminPosition(Long roomId, Long newAdminUserId, Long currentAdminUserId) {
        User currentAdmin = userRepository.findById(currentAdminUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        Membership currentAdminMembership = roomAccessGuard.requireAdmin(currentAdmin, room);

        if (newAdminUserId.equals(currentAdminUserId)) {
            throw new IllegalArgumentException("you are already a admin");
        }

        Membership newAdminMembership = membershipRepository
                .findByUser_IdAndRoom_IdAndMembershipStatus(newAdminUserId, roomId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("New admin must be current Active member of the room"));

        currentAdminMembership.setIsAdmin(false);
        newAdminMembership.setIsAdmin(true);

        membershipRepository.save(currentAdminMembership);
        membershipRepository.save(newAdminMembership);
    }
}