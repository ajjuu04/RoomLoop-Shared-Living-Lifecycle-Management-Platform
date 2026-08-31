package com.project.roomloop.security;

import com.project.roomloop.dto.*;
import com.project.roomloop.entity.Membership;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.MembershipStatus;
import com.project.roomloop.filter.JwtService;
import com.project.roomloop.repository.MembershipRepository;
import com.project.roomloop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
}
