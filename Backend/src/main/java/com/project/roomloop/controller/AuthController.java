package com.project.roomloop.controller;


import com.project.roomloop.dto.*;
import com.project.roomloop.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @PostMapping("/signin")     // this is basic for with email and pass but email can be not varify
    public ResponseEntity<SigninResponceDto> creteUser(@Valid @RequestBody SigninRequestDto signinRequestDto){
        return ResponseEntity.ok(authService.createNewUser(signinRequestDto));
    }

    @PostMapping("/login") // for basic unverified email and pass
    public ResponseEntity<LoginResponceDto> loginuser(@RequestBody LoginRequestDto loginRequestDto){
        return ResponseEntity.ok(authService.loginUser(loginRequestDto));
    }

     // new for the OTP

    @PostMapping("/request-otp")  //
    public ResponseEntity<Void> requestOtp(@RequestBody OtpRequestDto request) {
        authService.requestOtp(request.getMobileNumber());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-otp")
    public AuthResponseDto verifyOtp(@RequestBody OtpVerifyDto request) {
        log.info("Entering in Controller Layer");
        return authService.verifyOtp(request);
    }


}
