package com.project.roomloop.controller;

import com.project.roomloop.dto.*;
import com.project.roomloop.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/membership")
public class MembershipController {

    private final MembershipService membershipService;


    // this is for the Sercher who want to join room or the new user who want to join room
    @PostMapping("/{listingId}/join-request")
    public ResponseEntity<Void> createJoinRequest(
            @PathVariable Long listingId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("Entering to the mapping");
        membershipService.createJoinRequest(userId, listingId);
        log.info("return from the mapping");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    // this is for the Sercher who want to join room or the new user who want to join room
    @PostMapping("/{roomId}/join-request-room")
    public ResponseEntity<Void> createJoinRequestByRoomId(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId
    ) {
        membershipService.createJoinRequestByRoomId(userId, roomId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


//    below for others like sercher ansd member of the room to get member

    @GetMapping("/rooms/{roomId}/members")
    public RoomMembersDto getRoomMembers(@PathVariable Long roomId, @AuthenticationPrincipal Long userId) {
        return membershipService.getRoomMembers(roomId, userId);
    }

    @GetMapping("/rooms/{roomId}/join-requests")
    public List<JoinRequestsDto> getJoinRequests(@PathVariable Long roomId, @AuthenticationPrincipal Long userId) {
        return membershipService.getJoinRequests(roomId, userId);
    }

    @PostMapping("/memberships/{membershipId}/approve")
    public ResponseEntity<Void> approveJoinRequest(@PathVariable Long membershipId, @AuthenticationPrincipal Long userId) {
        membershipService.approveJoinRequest(membershipId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/memberships/{membershipId}/reject")
    public ResponseEntity<Void> rejectJoinRequest(@PathVariable Long membershipId, @AuthenticationPrincipal Long userId) {
        membershipService.rejectJoinRequest(membershipId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{roomId}/exit")
    public ResponseEntity<Void> exitRoom(@PathVariable Long roomId, @AuthenticationPrincipal Long userId) {
        membershipService.exitRoom(roomId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/rooms/{roomId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long roomId,
                                             @PathVariable Long userId,
                                             @AuthenticationPrincipal Long adminUserId) {
        membershipService.removeMember(roomId, userId, adminUserId);
        return ResponseEntity.noContent().build();
    }



    // make anohter membe admin

    @PostMapping("/memberships/{roomId}/transfer-admin/{newAdminUserId}")
    public ResponseEntity<Void> transferAdminPosition(@PathVariable Long roomId,
                                                      @PathVariable Long newAdminUserId,
                                                      @AuthenticationPrincipal Long currentAdminUserId){
        membershipService.transferAdminPosition(roomId,newAdminUserId,currentAdminUserId);
        return ResponseEntity.noContent().build();
    }


}