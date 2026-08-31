package com.project.roomloop.controller;


import com.project.roomloop.dto.LoginRequestDto;
import com.project.roomloop.dto.LoginResponceDto;
import com.project.roomloop.dto.SigninRequestDto;
import com.project.roomloop.dto.SigninResponceDto;
import com.project.roomloop.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<SigninResponceDto> creteUser(@Valid @RequestBody SigninRequestDto signinRequestDto){
        return ResponseEntity.ok(authService.createNewUser(signinRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponceDto> loginuser(@RequestBody LoginRequestDto loginRequestDto){
        return ResponseEntity.ok(authService.loginUser(loginRequestDto));
    }


}
