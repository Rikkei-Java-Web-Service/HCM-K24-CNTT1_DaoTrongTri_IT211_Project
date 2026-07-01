package com.re.project.controller.admin;

import com.re.project.dto.UserDetailsDto;
import com.re.project.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminService adminService;

    /**
     * Lấy danh sách User (có thể lọc theo role)
     */
    @GetMapping
    public ResponseEntity<List<UserDetailsDto>> getUserList(@RequestParam(required = false) String role) {
        return ResponseEntity.ok(adminService.getAllUsers(role));
    }

    /**
     * Khóa/Mở khóa tài khoản người dùng
     */
    @PutMapping("/{userId}/toggle-status")
    public ResponseEntity<UserDetailsDto> toggleUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.toggleUserEnabled(userId));
    }
}
