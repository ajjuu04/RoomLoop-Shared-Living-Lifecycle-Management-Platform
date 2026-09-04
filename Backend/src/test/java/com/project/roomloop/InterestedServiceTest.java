package com.project.roomloop;

import com.project.roomloop.dto.InterestedResponceDto;
import com.project.roomloop.entity.Interested;
import com.project.roomloop.entity.Listing;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.ListingStatus;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.repository.InterestedRepository;
import com.project.roomloop.repository.ListingRepository;
import com.project.roomloop.service.InterestedService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterestedServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private InterestedRepository interestedRepository;

    @Mock
    private HelperForRoomListing helperForRoomListing;

    @InjectMocks
    private InterestedService interestedService;


    //happy path
    @Test
    public void markUserInterestedInRoom() {

        Long userId = 1L;
        Long listingId = 10L;

        User user = User.builder()
                .id(userId)
                .name("Ajinkya")
                .mobileNumber("9876543210")
                .build();

        User memberListing = User.builder()
                .id(2L)
                .name("Rahul")
                .mobileNumber("9876543211")
                .build();

        Listing listing = Listing.builder()
                .id(listingId)
                .postedBy(memberListing)
                .listingStatus(ListingStatus.OPEN)
                .build();

        when(helperForRoomListing.checkUser(userId))
                .thenReturn(user);

        when(listingRepository.findById(listingId))
                .thenReturn(Optional.of(listing));

        when(interestedRepository.existsByUserAndListing(user, listing))
                .thenReturn(false);

        InterestedResponceDto result =
                interestedService.markUserInterestedInRoom(userId, listingId);

        assertNotNull(result);
        assertEquals("Rahul", result.getName());
        assertEquals("9876543211", result.getMobileNumber());

        verify(interestedRepository).save(any(Interested.class));
    }


    // listing not open
    @Test
    public void markUserInterestedInRoom_ShouldThrowException_WhenListingNotFound() {

        Long userId = 1L;
        Long listingId = 10L;

        User user = User.builder()
                .id(userId)
                .build();

        when(helperForRoomListing.checkUser(userId))
                .thenReturn(user);

        when(listingRepository.findById(listingId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> interestedService.markUserInterestedInRoom(userId, listingId)
        );

        verify(interestedRepository, never()).save(any(Interested.class));
    }


    @Test
    public void markUserInterestedInRoom_ShouldThrowException_WhenListingIsClosed() {

        Long userId = 1L;
        Long listingId = 10L;

        User user = User.builder()
                .id(userId)
                .build();

        User memberListing = User.builder()
                .id(2L)
                .build();

        Listing listing = Listing.builder()
                .id(listingId)
                .postedBy(memberListing)
                .listingStatus(ListingStatus.CLOSED)
                .build();

        when(helperForRoomListing.checkUser(userId))
                .thenReturn(user);

        when(listingRepository.findById(listingId))
                .thenReturn(Optional.of(listing));

        assertThrows(
                IllegalArgumentException.class,
                () -> interestedService.markUserInterestedInRoom(userId, listingId)
        );

        verify(interestedRepository, never()).existsByUserAndListing(any(), any());
        verify(interestedRepository, never()).save(any(Interested.class));
    }


    @Test
    public void markUserInterestedInRoom_ShouldThrowException_WhenAlreadyInterested() {

        Long userId = 1L;
        Long listingId = 10L;

        User user = User.builder()
                .id(userId)
                .build();

        User memberListing = User.builder()
                .id(2L)
                .build();

        Listing listing = Listing.builder()
                .id(listingId)
                .postedBy(memberListing)
                .listingStatus(ListingStatus.OPEN)
                .build();

        when(helperForRoomListing.checkUser(userId))
                .thenReturn(user);

        when(listingRepository.findById(listingId))
                .thenReturn(Optional.of(listing));

        when(interestedRepository.existsByUserAndListing(user, listing))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> interestedService.markUserInterestedInRoom(userId, listingId)
        );

        verify(interestedRepository, never()).save(any(Interested.class));
    }


    // mark user not interested
    // happy path

    @Test
    public void markUserNotInterestedInRoom_ShouldDeleteInterest_WhenInterestExists() {

        Long userId = 1L;
        Long listingId = 10L;

        User user = User.builder()
                .id(userId)
                .build();

        Listing listing = Listing.builder()
                .id(listingId)
                .build();

        Interested interested = Interested.builder()
                .user(user)
                .listing(listing)
                .build();

        when(helperForRoomListing.checkUser(userId))
                .thenReturn(user);

        when(listingRepository.findById(listingId))
                .thenReturn(Optional.of(listing));

        when(interestedRepository.findByUserAndListing(user, listing))
                .thenReturn(Optional.of(interested));

        interestedService.markUserNotInterestedInRoom(userId, listingId);

        verify(interestedRepository).delete(interested);
    }


    @Test
    public void markUserNotInterestedInRoom_ShouldThrowException_WhenListingNotFound() {

        Long userId = 1L;
        Long listingId = 10L;

        User user = User.builder()
                .id(userId)
                .build();

        when(helperForRoomListing.checkUser(userId))
                .thenReturn(user);

        when(listingRepository.findById(listingId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> interestedService.markUserNotInterestedInRoom(userId, listingId)
        );

        verify(interestedRepository, never()).delete(any(Interested.class));
    }


    @Test
    public void markUserNotInterestedInRoom_ShouldThrowException_WhenInterestNotFound() {

        Long userId = 1L;
        Long listingId = 10L;

        User user = User.builder()
                .id(userId)
                .build();

        Listing listing = Listing.builder()
                .id(listingId)
                .build();

        when(helperForRoomListing.checkUser(userId))
                .thenReturn(user);

        when(listingRepository.findById(listingId))
                .thenReturn(Optional.of(listing));

        when(interestedRepository.findByUserAndListing(user, listing))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> interestedService.markUserNotInterestedInRoom(userId, listingId)
        );

        verify(interestedRepository, never()).delete(any(Interested.class));
    }
}