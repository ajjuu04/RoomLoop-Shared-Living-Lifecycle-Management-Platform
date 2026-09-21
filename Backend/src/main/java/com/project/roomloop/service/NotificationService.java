package com.project.roomloop.service;

import com.project.roomloop.dto.NotificationDto;
import com.project.roomloop.entity.Notification;
import com.project.roomloop.entity.User;
import com.project.roomloop.error.AccessDeniedException;
import com.project.roomloop.error.ResourceNotFoundException;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final HelperForRoomListing helperForRoomListing;

    public void notify(User user, String message) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .build()
        );
    }

    public List<NotificationDto> getMyNotifications(Long userId) {
        User user = helperForRoomListing.checkUser(userId);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(n -> new NotificationDto(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        User user = helperForRoomListing.checkUser(userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("This notification does not belong to you");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}