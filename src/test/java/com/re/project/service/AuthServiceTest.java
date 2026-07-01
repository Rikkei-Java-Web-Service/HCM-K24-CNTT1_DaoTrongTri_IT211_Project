package com.re.project.service;

import com.re.project.dto.request.LoginRequest;
import com.re.project.dto.request.RegisterRequest;
import com.re.project.dto.response.LoginResponse;
import com.re.project.dto.response.RegisterResponse;
import com.re.project.entity.Role;
import com.re.project.entity.RoleEnum;
import com.re.project.entity.User;
import com.re.project.repository.RoleRepository;
import com.re.project.repository.UserRepository;
import com.re.project.security.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private com.re.project.mapper.UserMapper userMapper;

    @Mock
    private com.re.project.repository.RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private Role candidateRole;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("testuser");
        validRegisterRequest.setPassword("password");
        validRegisterRequest.setFullName("Test User");
        validRegisterRequest.setEmail("test@gmail.com");
        validRegisterRequest.setRole("CANDIDATE");

        candidateRole = new Role(1L, "ROLE_CANDIDATE", "Candidate", null);
    }

    // 1. Test Register Success
    @Test
    void register_Success() {
        when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findRoleByName("ROLE_CANDIDATE")).thenReturn(Optional.of(candidateRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = User.builder().id(1L).username("testuser").email("test@gmail.com").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.handleRegister(validRegisterRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(userRepository).save(any(User.class));
    }

    // 2. Test Register Duplicate Username
    @Test
    void register_DuplicateUsername_ThrowsException() {
        when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.of(new User()));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.handleRegister(validRegisterRequest));
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // 3. Test Register Duplicate Email
    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.handleRegister(validRegisterRequest));
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // 4. Test Login Success
    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        Authentication authentication = mock(Authentication.class);
        
        lenient().when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        lenient().when(tokenProvider.generateAccessToken(authentication)).thenReturn("access_token_123");
        lenient().when(tokenProvider.generateRefreshToken()).thenReturn("refresh_token_123");
        lenient().when(tokenProvider.getRefreshTokenExpirationMs()).thenReturn(900000L);

        User user = User.builder().id(1L).username("testuser").build();
        lenient().when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        lenient().when(authentication.getPrincipal()).thenReturn(user);
        lenient().when(userMapper.toUserLogin(any())).thenReturn(new com.re.project.dto.UserLoginDto());

        LoginResponse response = authService.handleLogin(loginRequest);

        assertNotNull(response);
        assertEquals("access_token_123", response.getAccessToken());
        assertEquals("refresh_token_123", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
    }

    // 5. Test Login Bad Credentials
    @Test
    void login_BadCredentials_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        lenient().when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.handleLogin(loginRequest));
    }
}
