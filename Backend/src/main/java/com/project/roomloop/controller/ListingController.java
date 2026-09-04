package com.project.roomloop.controller;


import com.project.roomloop.dto.InterestedResponceDto;
import com.project.roomloop.dto.ListingDetailsDto;
import com.project.roomloop.dto.ListingUpdateDto;
import com.project.roomloop.dto.RegisterNewListingRequest;
import com.project.roomloop.entity.User;
import com.project.roomloop.service.InterestedService;
import com.project.roomloop.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/listing")
@RequiredArgsConstructor
@Slf4j
public class ListingController {

    private final ListingService listingService;
    private final InterestedService interestedService;

    @PostMapping("/list/{roomId}")
    public ResponseEntity<ListingDetailsDto> ListNewListing(@RequestBody RegisterNewListingRequest registerNewListingRequest, @PathVariable Long roomId, @AuthenticationPrincipal Long userId){
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.ListNewListing(registerNewListingRequest,roomId,userId));
    }

    @GetMapping("/mypost/{roomId}")
    public ResponseEntity<ListingDetailsDto> getMyListing(@PathVariable Long roomId, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(listingService.getMyListing(roomId,userId));
    }

    @PatchMapping("/edit/{roomId}")
    public ResponseEntity<ListingDetailsDto> editMyListing(@RequestBody ListingUpdateDto listingUpdateDto, @PathVariable Long roomId, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(listingService.editMyListing(listingUpdateDto,roomId,userId));
    }

    @DeleteMapping("/delete/{roomId}")
    public ResponseEntity<ListingDetailsDto> deleteMyListing(@PathVariable Long roomId, @AuthenticationPrincipal Long userId){
        return ResponseEntity.status(HttpStatus.OK).body(listingService.deleteMyListing(roomId,userId));
    }




    // bellow Api for seracher



    @GetMapping
    public ResponseEntity<List<ListingDetailsDto>> getAllListings(
            @RequestParam(value = "page", defaultValue = "0")Integer pageNumber,
            @RequestParam(value = "size", defaultValue = "10")Integer pagesize
    ){
        return ResponseEntity.ok(listingService.getAllListings(pageNumber,pagesize));
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<ListingDetailsDto> getListingById(@PathVariable Long listingId){
        return ResponseEntity.ok(listingService.getListingById(listingId));
    }

    @PostMapping("/interest/{listingId}")
    public ResponseEntity<InterestedResponceDto> markUserInterestedInRoom(@AuthenticationPrincipal Long userId,@PathVariable Long listingId){
        return ResponseEntity.ok(interestedService.markUserInterestedInRoom(userId,listingId));
    }


    @DeleteMapping("/interest/{listingId}")
    public ResponseEntity<Void> markUserNotInterestedInRoom(@AuthenticationPrincipal Long userId,
                                                            @PathVariable Long listingId) {
        interestedService.markUserNotInterestedInRoom(userId, listingId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
