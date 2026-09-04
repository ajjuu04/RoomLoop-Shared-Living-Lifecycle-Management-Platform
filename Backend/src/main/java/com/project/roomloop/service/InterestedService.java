package com.project.roomloop.service;

import com.project.roomloop.dto.InterestedResponceDto;
import com.project.roomloop.entity.Interested;
import com.project.roomloop.entity.Listing;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.ListingStatus;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.repository.InterestedRepository;
import com.project.roomloop.repository.ListingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterestedService {

    private final ListingRepository listingRepository;
    private final InterestedRepository interestedRepository;
    private final HelperForRoomListing helperForRoomListing;

    @Transactional
    public InterestedResponceDto markUserInterestedInRoom(Long userId, Long listingId) {

        User user = helperForRoomListing.checkUser(userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Listing with this id not availble"
                ));

        User memberListing = listing.getPostedBy();

        if (listing.getListingStatus() != ListingStatus.OPEN) {
            throw new IllegalArgumentException(
                    "this Listing is now CLosed or Filled Pleas echeck new another Listing"
            );
        }

        boolean alreadyInterested =
                interestedRepository.existsByUserAndListing(user, listing);

        if (alreadyInterested) {
            throw new IllegalStateException(
                    "You already marked interest in this listing"
            );
        }

        interestedRepository.save(
                Interested.builder()
                        .user(user)
                        .listing(listing)
                        .build()
        );

        return new InterestedResponceDto(memberListing.getName(),memberListing.getMobileNumber());
    }


    public void markUserNotInterestedInRoom(Long userId, Long listingId) {

        User user = helperForRoomListing.checkUser(userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Listing with this id not availble"
                ));

        Interested interested = interestedRepository.findByUserAndListing(user, listing)
                .orElseThrow(() -> new IllegalStateException("You haven't marked interest in this listing"));

        interestedRepository.delete(interested);
    }
}