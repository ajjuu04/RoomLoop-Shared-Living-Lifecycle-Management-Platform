package com.project.roomloop.security;

import com.project.roomloop.dto.*;
import com.project.roomloop.entity.Membership;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.MembershipStatus;
import com.project.roomloop.filter.JwtService;
import com.project.roomloop.repository.MembershipRepository;
import com.project.roomloop.repository.UserRepository;
import com.project.roomloop.service.SmsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final MembershipRepository membershipRepository;

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SmsService smsService;

    public SigninResponceDto createNewUser(SigninRequestDto signinRequestDto) {
        log.info("Inside of the metthod ");
        User user = userRepository.findByEmail(signinRequestDto.getEmail()).orElse(null);

        if (user!=null) {
            throw new IllegalArgumentException(
                    "User already exists, please login"
            );
        }
        User newUser = User.builder()
                .email(signinRequestDto.getEmail())
                .name(signinRequestDto.getName())
                .password(passwordEncoder.encode(signinRequestDto.getPassword()))
                .mobileNumber(signinRequestDto.getMobileNumber())
                .build();

        userRepository.save(newUser);

        return modelMapper.map(newUser, SigninResponceDto.class);
    }

    public LoginResponceDto loginUser(LoginRequestDto loginRequestDto) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(),loginRequestDto.getPassword()));

        User user = (User) authentication.getPrincipal();
        String jwtToken = jwtService.generateAccessToken(user);

        return new LoginResponceDto(user.getId(),user.getEmail(),jwtToken);
    }


    public UserProfileDto getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("user not found with this id please create Ac"));

        Optional<Membership> activeMembership = membershipRepository.findByUserAndMembershipStatus(user, MembershipStatus.ACTIVE);

        if (activeMembership.isEmpty()){
            return UserProfileDto.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role("SEARCHER")
                    .build();
        }

        Membership membership = activeMembership.get();
        String role = membership.getIsAdmin() ? "ADMIN" : "MEMBER";

        return UserProfileDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(role)
                .roomId(membership.getRoom().getId())
                .build();
    }

    public void requestOtp(String mobileNumber){
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .mobileNumber(mobileNumber)
                                .build()));


        if (user.getOtpExpiresAt() != null && user.getOtpExpiresAt().minusMinutes(4).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Please wait before requesting another code wait for ");
        }

        String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);

        user.setOtpCode(passwordEncoder.encode(otp));
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(5));
        user.setOtpAttempts(0);

        userRepository.save(user);

        smsService.send(mobileNumber, "Your RoomLoop OTP code is : " + otp);

    }


    public AuthResponseDto verifyOtp(OtpVerifyDto request) {
        log.info("Entering in services layer");
        User user = userRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> new IllegalArgumentException("there is no account with this number please request a code first"));

        if (user.getOtpCode() == null) {
            throw new IllegalArgumentException("No active code request a new one");
        }
        if (user.getOtpAttempts() >= 5) {
            throw new IllegalStateException("Too many wrong attempts request a new code");
        }
        if (user.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Code expired request a new one");
        }
        if (!passwordEncoder.matches(request.getOtp(), user.getOtpCode())) {
            user.setOtpAttempts(user.getOtpAttempts() + 1);
            userRepository.save(user);
            throw new IllegalArgumentException("Wrong code");
        }

        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        user.setOtpAttempts(0);
        userRepository.save(user);

        return new AuthResponseDto(user.getId(),jwtService.generateAccessToken(user));
    }




}
