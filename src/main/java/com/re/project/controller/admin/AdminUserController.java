package com.re.project.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.re.project.dto.UserDetailsDto;
import com.re.project.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDetailsDto>> getUserList(@org.springframework.web.bind.annotation.RequestParam(required = false) String role) {
        return ResponseEntity.ok(userService.getUsers(role));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable Long userId,
                                                   @PathVariable Long roleId) {
        userService.removeRole(userId, roleId);
        return ResponseEntity.noContent().build();
    }
}
