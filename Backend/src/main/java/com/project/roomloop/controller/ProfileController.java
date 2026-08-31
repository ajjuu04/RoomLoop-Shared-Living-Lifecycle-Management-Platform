package com.project.roomloop.controller;


import com.project.roomloop.dto.UserProfileDto;
import com.project.roomloop.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ProfileController {

    private final AuthService authService;

    @GetMapping("/me/{userId}")
    public ResponseEntity<UserProfileDto> getMyProfile(@PathVariable Long userId){
        return ResponseEntity.ok(authService.getMyProfile(userId));
    }
}
