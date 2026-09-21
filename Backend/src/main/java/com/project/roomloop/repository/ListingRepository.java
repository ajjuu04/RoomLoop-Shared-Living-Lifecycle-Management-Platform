package com.project.roomloop.repository;

import com.project.roomloop.entity.Listing;
import com.project.roomloop.entity.Room;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.ListingStatus;
import com.project.roomloop.entity.types.MembershipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    boolean existsByRoomAndPostedByAndListingStatus(
            Room room,
            User postedBy,
            ListingStatus listingStatus
    );

    Optional<Listing> findByPostedBy(
            User postedBy
    );
    
    


    Page<Listing> findByListingStatus(
            ListingStatus listingStatus,
            Pageable pageable);


    void deleteByPostedByAndListingStatus(User user, ListingStatus listingStatus);
}