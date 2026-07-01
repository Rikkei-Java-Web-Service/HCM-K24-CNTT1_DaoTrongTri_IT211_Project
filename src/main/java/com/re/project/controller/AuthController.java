package com.re.project.controller;

import com.re.project.dto.request.*;
import com.re.project.dto.response.LoginResponse;
import com.re.project.dto.response.RegisterResponse;
import com.re.project.security.TokenProvider;
import com.re.project.service.AuthService;
import com.re.project.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final TokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Đăng nhập (cấp Access Token + Refresh Token)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.handleLogin(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Đăng ký tài khoản (BCrypt)
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = authService.handleRegister(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    /**
     * Xoay vòng Token (Refresh Token Rotation)
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        LoginResponse loginResponse = authService.handleRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Đăng xuất (Đưa Access Token vào Blacklist Database)
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            long remainingTime = tokenProvider.getRemainingTime(token);
            tokenBlacklistService.addToBlacklist(token, remainingTime);
        }
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    /**
     * Đổi mật khẩu
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        authService.changePassword(authentication.getName(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Quên mật khẩu
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String tempPassword = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "A temporary password has been generated",
                "temporaryPassword", tempPassword
        ));
    }
}
