package com.project.roomloop.service;


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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingService {
    private final ListingRepository listingRepository;
    private final RoomAccessGuide roomAccessGuide;
    private final RoomRepository roomRepository;
    private final HelperForRoomListing helperForRoomListing;
    private final ModelMapper modelMapper;


    @Transactional
    public ListingDetailsDto ListNewListing(RegisterNewListingRequest registerNewListingRequest, Long roomId, Long userId) {

        User user = helperForRoomListing.checkUser(userId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room with this room id not found"));

        Membership membership = roomAccessGuide.isUserActiveMemberOfRoom(user, room);

        boolean isAdmin = membership.getIsAdmin();

        boolean isUserHasAnyOpenListing =
                listingRepository.existsByRoomAndPostedByAndListingStatus(
                        room, user, ListingStatus.OPEN
                );

        if (isUserHasAnyOpenListing) {
            throw new IllegalStateException("You already have a open listing for this room");
        }

        Listing listing = Listing.builder()
                .room(room)
                .postedBy(user)
                .preferences(registerNewListingRequest.getPreferences())
                .listingStatus(ListingStatus.OPEN)
                .updatedAt(LocalDateTime.now())
                .build();

        if (isAdmin && registerNewListingRequest.getOpenSpots() > 0) {
            listing.setOpenSpots(registerNewListingRequest.getOpenSpots());
        } else {
            listing.setOpenSpots(1);
        }

        Listing newListing = listingRepository.save(listing);

        return mapToDto(newListing);
    }


    public ListingDetailsDto getMyListing(Long roomId, Long userId) {

        User user = helperForRoomListing.checkUser(userId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room with this room id not found"));

        Membership membership = roomAccessGuide.isUserActiveMemberOfRoom(user, room);

        Listing listing = listingRepository.findByPostedBy(user)
                .orElseThrow(() -> new EntityNotFoundException("still you didnt post list it please list first"));

        return mapToDto(listing);
    }


    public ListingDetailsDto deleteMyListing(Long roomId, Long userId) {

        User user = helperForRoomListing.checkUser(userId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room with this room id not found"));

        Membership membership = roomAccessGuide.isUserActiveMemberOfRoom(user, room);

        Listing listing = listingRepository.findByPostedBy(user)
                .orElseThrow(() -> new EntityNotFoundException("you dot have any listing to delete"));

        listing.setListingStatus(ListingStatus.CLOSED);

        Listing newListing = listingRepository.save(listing);

        return mapToDto(listing);
    }


    public ListingDetailsDto editMyListing(ListingUpdateDto listingUpdateDto, Long roomId, Long userId) {

        User user = helperForRoomListing.checkUser(userId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room with this room id not found"));

        Membership membership = roomAccessGuide.isUserActiveMemberOfRoom(user, room);

        Listing listing = listingRepository.findByPostedBy(user)
                .orElseThrow(() -> new EntityNotFoundException("you dot have any listing to delete"));

        listing.setPreferences(listingUpdateDto.getPreferences());
        listing.setUpdatedAt(LocalDateTime.now());

        Listing newListing = listingRepository.save(listing);

        return mapToDto(newListing);
    }


    ///     //////////////////////////////////////////////////////////////////////////////////
    ///     //////////////////////////////////////////////////           /////////////////////
    ///     ////                                                // \\                    ////
    ///     ////                                               //   \\                   ////
    ///     ////                                              //     \\                  ////
    ///     ////             below for the                   //////\\\\\                 ////
    ///     ////              sercher API                   //         \\                ////
    ///     ////                                           //           \\               ////
    ///     ////                                          //             \\              ////
    ///     ///////////////////////////////////////////                       ///////////////
    ///     /////////////////////////////////////////////////////////////////////////////////


    public List<ListingDetailsDto> getAllListings(Integer pageNumber, Integer pagesize) {
        return listingRepository.findByListingStatus(
                        ListingStatus.OPEN,
                        PageRequest.of(pageNumber, pagesize)
                )
                .stream()
                .map(listing -> mapToDto(listing))
                .collect(Collectors.toList());
    }


    private ListingDetailsDto mapToDto(Listing listing) {

        Room room = listing.getRoom();

        return ListingDetailsDto.builder()
                .id(listing.getId())
                .room(room.getId())
                .postedBy(listing.getPostedBy().getId())
                .address(room.getAddress())
                .openSpots(listing.getOpenSpots())
                .preferences(listing.getPreferences())
                .listingStatus(listing.getListingStatus())
                .listedAt(listing.getListedAt())
                .updatedAt(listing.getUpdatedAt())
                .rent(helperForRoomListing.getCurrentRentPerMemberForListing(room))
                .deposit(helperForRoomListing.getCurrentDepositPerMemberForListing(room))
                .totalOccupancy(room.getTotalOccupancy())
                .build();
    }


    public ListingDetailsDto getListingById(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("ther ein no any Listing wiith this Id"));

        return mapToDto(listing);
    }
}