package com.project.roomloop;

import com.project.roomloop.dto.NotificationDto;
import com.project.roomloop.entity.Notification;
import com.project.roomloop.entity.User;
import com.project.roomloop.error.AccessDeniedException;
import com.project.roomloop.error.ResourceNotFoundException;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.repository.NotificationRepository;
import com.project.roomloop.service.NotificationService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private HelperForRoomListing helperForRoomListing;


    // =========================================================
    // notify()
    // =========================================================

    @Nested
    class NotifyTests {

        @Test
        void happyPath_NotifyTests() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            String message = "New join request received";


            notificationService.notify(user, message);


            ArgumentCaptor<Notification> notificationCaptor =
                    ArgumentCaptor.forClass(Notification.class);

            verify(notificationRepository)
                    .save(notificationCaptor.capture());

            Notification notification =
                    notificationCaptor.getValue();


            assertEquals(user, notification.getUser());

            assertEquals(
                    message,
                    notification.getMessage()
            );

            assertFalse(notification.isRead());
        }
    }


    // =========================================================
    // getMyNotifications()
    // =========================================================

    @Nested
    class GetMyNotificationsTests {

        @Test
        void happyPath_GetMyNotificationsTests() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            LocalDateTime date1 =
                    LocalDateTime.of(2026, 9, 21, 10, 30);

            LocalDateTime date2 =
                    LocalDateTime.of(2026, 9, 20, 15, 30);

            Notification notification1 = Notification.builder()
                    .id(101L)
                    .user(user)
                    .message("New join request received")
                    .isRead(false)
                    .createdAt(date1)
                    .build();

            Notification notification2 = Notification.builder()
                    .id(102L)
                    .user(user)
                    .message("Your join request was approved")
                    .isRead(true)
                    .createdAt(date2)
                    .build();


            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(notificationRepository
                    .findByUserOrderByCreatedAtDesc(user))
                    .thenReturn(List.of(
                            notification1,
                            notification2
                    ));


            List<NotificationDto> response =
                    notificationService.getMyNotifications(userId);


            assertNotNull(response);

            assertEquals(2, response.size());


            assertEquals(
                    101L,
                    response.get(0).getId()
            );

            assertEquals(
                    "New join request received",
                    response.get(0).getMessage()
            );

            assertFalse(
                    response.get(0).isRead()
            );

            assertEquals(
                    date1,
                    response.get(0).getCreatedAt()
            );


            assertEquals(
                    102L,
                    response.get(1).getId()
            );

            assertEquals(
                    "Your join request was approved",
                    response.get(1).getMessage()
            );

            assertTrue(
                    response.get(1).isRead()
            );


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(notificationRepository)
                    .findByUserOrderByCreatedAtDesc(user);
        }


        @Test
        void userNotFound() {

            Long userId = 1L;

            when(helperForRoomListing.checkUser(userId))
                    .thenThrow(
                            new ResourceNotFoundException(
                                    "User not found with this id"
                            )
                    );


            assertThrows(
                    ResourceNotFoundException.class,
                    () -> notificationService
                            .getMyNotifications(userId)
            );


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(notificationRepository, never())
                    .findByUserOrderByCreatedAtDesc(any(User.class));
        }


        @Test
        void noNotifications() {

            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();


            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(notificationRepository
                    .findByUserOrderByCreatedAtDesc(user))
                    .thenReturn(List.of());


            List<NotificationDto> response =
                    notificationService.getMyNotifications(userId);


            assertNotNull(response);

            assertTrue(response.isEmpty());


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(notificationRepository)
                    .findByUserOrderByCreatedAtDesc(user);
        }
    }


    // =========================================================
    // markAsRead()
    // =========================================================

    @Nested
    class MarkAsReadTests {

        @Test
        void happyPath_markAsRead() {

            Long notificationId = 101L;
            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();

            Notification notification = Notification.builder()
                    .id(notificationId)
                    .user(user)
                    .message("New join request received")
                    .isRead(false)
                    .build();


            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(notificationRepository.findById(notificationId))
                    .thenReturn(Optional.of(notification));


            notificationService.markAsRead(
                    notificationId,
                    userId
            );


            assertTrue(notification.isRead());


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(notificationRepository)
                    .findById(notificationId);

            verify(notificationRepository)
                    .save(notification);
        }


        @Test
        void userNotFound() {

            Long notificationId = 101L;
            Long userId = 1L;


            when(helperForRoomListing.checkUser(userId))
                    .thenThrow(
                            new ResourceNotFoundException(
                                    "User not found with this id"
                            )
                    );


            assertThrows(
                    ResourceNotFoundException.class,
                    () -> notificationService.markAsRead(
                            notificationId,
                            userId
                    )
            );


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(notificationRepository, never())
                    .findById(anyLong());

            verify(notificationRepository, never())
                    .save(any(Notification.class));
        }


        @Test
        void notificationNotFound() {

            Long notificationId = 101L;
            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .build();


            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(notificationRepository.findById(notificationId))
                    .thenReturn(Optional.empty());


            assertThrows(
                    ResourceNotFoundException.class,
                    () -> notificationService.markAsRead(
                            notificationId,
                            userId
                    )
            );


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(notificationRepository)
                    .findById(notificationId);

            verify(notificationRepository, never())
                    .save(any(Notification.class));
        }


        @Test
        void notificationBelongsToAnotherUser() {

            Long notificationId = 101L;
            Long userId = 1L;
            Long anotherUserId = 2L;

            User user = User.builder()
                    .id(userId)
                    .build();

            User anotherUser = User.builder()
                    .id(anotherUserId)
                    .build();

            Notification notification = Notification.builder()
                    .id(notificationId)
                    .user(anotherUser)
                    .message("Private notification")
                    .isRead(false)
                    .build();


            when(helperForRoomListing.checkUser(userId))
                    .thenReturn(user);

            when(notificationRepository.findById(notificationId))
                    .thenReturn(Optional.of(notification));


            assertThrows(
                    AccessDeniedException.class,
                    () -> notificationService.markAsRead(
                            notificationId,
                            userId
                    )
            );


            assertFalse(notification.isRead());


            verify(helperForRoomListing)
                    .checkUser(userId);

            verify(notificationRepository)
                    .findById(notificationId);

            verify(notificationRepository, never())
                    .save(any(Notification.class));
        }
    }
}