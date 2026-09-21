package com.project.roomloop.controller;

import com.project.roomloop.dto.NotificationDto;
import com.project.roomloop.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getMyNotifications(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.getMyNotifications(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }
}