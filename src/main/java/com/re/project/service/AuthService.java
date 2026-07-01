package com.re.project.service;

import com.re.project.dto.UserLoginDto;
import com.re.project.dto.request.LoginRequest;
import com.re.project.dto.request.RegisterRequest;
import com.re.project.dto.response.LoginResponse;
import com.re.project.dto.response.RegisterResponse;
import com.re.project.entity.RefreshToken;
import com.re.project.entity.Role;
import com.re.project.entity.User;
import com.re.project.entity.UserRole;
import com.re.project.exception.ResourceNotFoundException;
import com.re.project.mapper.UserMapper;
import com.re.project.repository.RefreshTokenRepository;
import com.re.project.repository.RoleRepository;
import com.re.project.repository.UserRepository;
import com.re.project.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.access-expires-in-mil-seconds}")
    private long accessTokenExpirationMs;

    /**
     * Login cấp cả Access Token + Refresh Token
     */
    @Transactional
    public LoginResponse handleLogin(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken currentUser = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );
        Authentication authentication = authenticationManager.authenticate(currentUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate Access Token (JWT)
        String accessToken = tokenProvider.generateAccessToken(authentication);

        // Generate Refresh Token (UUID) and save to DB
        User user = userRepository.findUserByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String refreshTokenStr = tokenProvider.generateRefreshToken();
        long refreshExpMs = tokenProvider.getRefreshTokenExpirationMs();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiresAt(LocalDateTime.now().plusNanos(refreshExpMs * 1_000_000))
                .build();
        refreshTokenRepository.save(refreshToken);

        Object principal = authentication.getPrincipal();
        UserLoginDto userLoginDto = userMapper.toUserLogin(principal);

        return LoginResponse.builder()
                .message("Login successfully!")
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs)
                .user(userLoginDto)
                .build();
    }

    /**
     * Xoay vòng Token (Refresh Token Rotation)
     * Dùng Refresh Token cũ → nhận Access Token mới + Refresh Token mới
     */
    @Transactional
    public LoginResponse handleRefreshToken(String oldRefreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(oldRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token is invalid or has been revoked"));

        // Check expiration
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new RuntimeException("Refresh token has expired. Please login again");
        }

        // Revoke old refresh token (Rotation)
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();

        // Generate new Access Token
        String newAccessToken = tokenProvider.generateAccessTokenFromUsername(user.getUsername());

        // Generate new Refresh Token
        String newRefreshTokenStr = tokenProvider.generateRefreshToken();
        long refreshExpMs = tokenProvider.getRefreshTokenExpirationMs();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenStr)
                .user(user)
                .expiresAt(LocalDateTime.now().plusNanos(refreshExpMs * 1_000_000))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        UserLoginDto userLoginDto = userMapper.toUserLogin(user);

        return LoginResponse.builder()
                .message("Token refreshed successfully!")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs)
                .user(userLoginDto)
                .build();
    }

    /**
     * Đăng ký tài khoản (BCrypt)
     */
    @Transactional
    public RegisterResponse handleRegister(RegisterRequest request) {
        if (userRepository.findUserByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .enabled(true)
                .build();

        // Assign role
        String roleName = "ROLE_" + request.getRole().toUpperCase();
        Role role = roleRepository.findRoleByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();

        user.setUserRoles(Set.of(userRole));
        userRepository.save(user);

        return RegisterResponse.builder()
                .message("Registration successful")
                .username(user.getUsername())
                .build();
    }

    /**
     * Đổi mật khẩu
     */
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    /**
     * Quên mật khẩu (đặt lại bằng email)
     * Tạm thời: tìm user theo email, sinh mật khẩu mới tạm thời
     */
    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        // Generate a temporary password
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByUserId(user.getId());

        return tempPassword;
    }
}
