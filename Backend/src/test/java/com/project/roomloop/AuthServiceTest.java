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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private MembershipRepository membershipRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private SmsService smsService;

    // =========================================================
    // createNewUser()
    // =========================================================

    @Nested
    class CreateNewUserTests {

        @Test
        void happyPath() {
            SigninRequestDto request = new SigninRequestDto(
                    "priya@test.com", "Priya", "password123", "9876543210"
            );
            
            when(userRepository.findByEmail("priya@test.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

            SigninResponceDto expectedDto = new SigninResponceDto();
            when(modelMapper.map(any(User.class), eq(SigninResponceDto.class))).thenReturn(expectedDto);

            SigninResponceDto response = authService.createNewUser(request);

            assertNotNull(response);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals("priya@test.com", savedUser.getEmail());
            assertEquals("Priya", savedUser.getName());
            assertEquals("hashedPassword", savedUser.getPassword());
            assertEquals("9876543210", savedUser.getMobileNumber());

            verify(modelMapper).map(savedUser, SigninResponceDto.class);
        }

        @Test
        void emailAlreadyExists_throws() {
            SigninRequestDto request = new SigninRequestDto(
                    "priya@test.com", "Priya", "password123", "9876543210"
            );

            when(userRepository.findByEmail("priya@test.com"))
                    .thenReturn(Optional.of(User.builder().id(1L).email("priya@test.com").build()));

            assertThrows(IllegalArgumentException.class, () -> authService.createNewUser(request));

            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    // =========================================================
    // loginUser()
    // =========================================================

    @Nested
    class LoginUserTests {

        @Mock private Authentication authentication;

        @Test
        void happyPath() {
            LoginRequestDto request = new LoginRequestDto("priya@test.com", "password123");
            User user = User.builder().id(1L).email("priya@test.com").build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(jwtService.generateAccessToken(user)).thenReturn("jwt-token-123");

            LoginResponceDto response = authService.loginUser(request);

            assertEquals(1L, response.getId());
            assertEquals("priya@test.com", response.getEmail());
            assertEquals("jwt-token-123", response.getJwtToken());

            verify(jwtService).generateAccessToken(user);
        }

        @Test
        void invalidCredentials_propagatesException() {
            LoginRequestDto request = new LoginRequestDto("priya@test.com", "wrongPassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.loginUser(request));

            verify(jwtService, never()).generateAccessToken(any(User.class));
        }
    }

    // =========================================================
    // getMyProfile()
    // =========================================================

    @Nested
    class GetMyProfileTests {

        @Test
        void noActiveMembership_returnsSearcherRole() {
            Long userId = 1L;
            User user = User.builder().id(userId).name("Priya").email("priya@test.com").build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(membershipRepository.findByUserAndMembershipStatus(user, MembershipStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            UserProfileDto profile = authService.getMyProfile(userId);

            assertEquals("SEARCHER", profile.getRole());
            assertNull(profile.getRoomId());
            assertEquals("Priya", profile.getName());
        }

        @Test
        void activeNonAdminMembership_returnsMemberRoleAndRoomId() {
            Long userId = 1L;
            User user = User.builder().id(userId).name("Priya").build();
            com.project.roomloop.entity.Room room = com.project.roomloop.entity.Room.builder().id(50L).build();
            Membership membership = Membership.builder().room(room).isAdmin(false).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(membershipRepository.findByUserAndMembershipStatus(user, MembershipStatus.ACTIVE))
                    .thenReturn(Optional.of(membership));

            UserProfileDto profile = authService.getMyProfile(userId);

            assertEquals("MEMBER", profile.getRole());
            assertEquals(50L, profile.getRoomId());
        }

        @Test
        void activeAdminMembership_returnsAdminRole() {
            Long userId = 1L;
            User user = User.builder().id(userId).build();
            com.project.roomloop.entity.Room room = com.project.roomloop.entity.Room.builder().id(50L).build();
            Membership membership = Membership.builder().room(room).isAdmin(true).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(membershipRepository.findByUserAndMembershipStatus(user, MembershipStatus.ACTIVE))
                    .thenReturn(Optional.of(membership));

            UserProfileDto profile = authService.getMyProfile(userId);

            assertEquals("ADMIN", profile.getRole());
        }

        @Test
        void userNotFound_throws() {
            // NOTE: tests current behavior (EntityNotFoundException).
            // Update to ResourceNotFoundException once that bug is fixed.
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> authService.getMyProfile(99L));
        }
    }

    // =========================================================
    // requestOtp()
    // =========================================================

    @Nested
    class RequestOtpTests {

        @Test
        void newNumber_createsUserAndSendsOtp() {
            String mobileNumber = "9876543210";

            when(userRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");

            authService.requestOtp(mobileNumber);

            verify(smsService).send(eq(mobileNumber), contains("RoomLoop OTP"));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(2)).save(userCaptor.capture());

            User finalSavedState = userCaptor.getAllValues().get(1);
            assertEquals("hashedOtp", finalSavedState.getOtpCode());
            assertEquals(0, finalSavedState.getOtpAttempts());
            assertNotNull(finalSavedState.getOtpExpiresAt());
        }

        @Test
        void existingNumberNoActiveCode_sendsNewOtp() {
            String mobileNumber = "9876543210";
            User existingUser = User.builder().id(1L).mobileNumber(mobileNumber).build();

            when(userRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");

            authService.requestOtp(mobileNumber);

            verify(smsService).send(eq(mobileNumber), anyString());
            verify(userRepository, times(1)).save(existingUser);
        }

        @Test
        void cooldownActive_throwsAndDoesNotSendSms() {
            String mobileNumber = "9876543210";
            User existingUser = User.builder()
                    .id(1L).mobileNumber(mobileNumber)
                    .otpExpiresAt(LocalDateTime.now().plusMinutes(5)) // just requested
                    .build();

            when(userRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.of(existingUser));

            assertThrows(IllegalStateException.class, () -> authService.requestOtp(mobileNumber));

            verify(smsService, never()).send(anyString(), anyString());
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    // =========================================================
    // verifyOtp()
    // =========================================================

    @Nested
    class VerifyOtpTests {

        @Test
        void happyPath_returnsTokenAndClearsOtp() {
            OtpVerifyDto request = new OtpVerifyDto("9876543210", "123456");
            User user = User.builder()
                    .id(1L).mobileNumber("9876543210")
                    .otpCode("hashedOtp").otpExpiresAt(LocalDateTime.now().plusMinutes(3))
                    .otpAttempts(0)
                    .build();

            when(userRepository.findByMobileNumber("9876543210")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("123456", "hashedOtp")).thenReturn(true);
            when(jwtService.generateAccessToken(user)).thenReturn("jwt-token-456");

            AuthResponseDto response = authService.verifyOtp(request);

            assertEquals(1L, response.getUserId());
            assertEquals("jwt-token-456", response.getJwtToken());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertNull(savedUser.getOtpCode());
            assertNull(savedUser.getOtpExpiresAt());
            assertEquals(0, savedUser.getOtpAttempts());
        }

        @Test
        void noAccountWithNumber_throws() {
            OtpVerifyDto request = new OtpVerifyDto("0000000000", "123456");

            when(userRepository.findByMobileNumber("0000000000")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> authService.verifyOtp(request));
        }

        @Test
        void noActiveCode_throws() {
            OtpVerifyDto request = new OtpVerifyDto("9876543210", "123456");
            User user = User.builder().id(1L).mobileNumber("9876543210").otpCode(null).build();

            when(userRepository.findByMobileNumber("9876543210")).thenReturn(Optional.of(user));

            assertThrows(IllegalArgumentException.class, () -> authService.verifyOtp(request));
        }

        @Test
        void tooManyAttempts_throws() {
            OtpVerifyDto request = new OtpVerifyDto("9876543210", "123456");
            User user = User.builder()
                    .id(1L).mobileNumber("9876543210")
                    .otpCode("hashedOtp").otpAttempts(5)
                    .build();

            when(userRepository.findByMobileNumber("9876543210")).thenReturn(Optional.of(user));

            assertThrows(IllegalStateException.class, () -> authService.verifyOtp(request));

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        void expiredCode_throws() {
            OtpVerifyDto request = new OtpVerifyDto("9876543210", "123456");
            User user = User.builder()
                    .id(1L).mobileNumber("9876543210")
                    .otpCode("hashedOtp").otpAttempts(0)
                    .otpExpiresAt(LocalDateTime.now().minusMinutes(1)) // already expired
                    .build();

            when(userRepository.findByMobileNumber("9876543210")).thenReturn(Optional.of(user));

            assertThrows(IllegalArgumentException.class, () -> authService.verifyOtp(request));
        }

        @Test
        void wrongCode_incrementsAttemptsAndThrows() {
            OtpVerifyDto request = new OtpVerifyDto("9876543210", "999999");
            User user = User.builder()
                    .id(1L).mobileNumber("9876543210")
                    .otpCode("hashedOtp").otpAttempts(0)
                    .otpExpiresAt(LocalDateTime.now().plusMinutes(3))
                    .build();

            when(userRepository.findByMobileNumber("9876543210")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("999999", "hashedOtp")).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> authService.verifyOtp(request));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(1, userCaptor.getValue().getOtpAttempts());

            verify(jwtService, never()).generateAccessToken(any(User.class));
        }
    }
}