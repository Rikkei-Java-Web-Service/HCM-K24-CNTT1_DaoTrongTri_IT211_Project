package com.re.project.mapper;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.re.project.dto.UserDetailsDto;
import com.re.project.dto.UserLoginDto;
import com.re.project.dto.UserProfileDto;
import com.re.project.dto.response.UserUpdateResponse;
import com.re.project.entity.User;
import com.re.project.entity.UserRole;
import com.re.project.security.UserDetailsImpl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserLoginDto toUserLogin(Object userPrincipal) {
        if (userPrincipal instanceof UserDetailsImpl userDetails) {
            return UserLoginDto.builder()
                    .id(userDetails.getId())
                    .email(userDetails.getEmail())
                    .username(userDetails.getUsername())
                    .fullName(userDetails.getFullName())
                    .roles(userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toSet()))
                    .lastLogin(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .build();
        }
        return null;
    }

    public UserLoginDto toUserLogin(User user) {
        if (user == null) return null;
        return UserLoginDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(mapUserRolesToStrings(user.getUserRoles()))
                .lastLogin(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    public UserDetailsDto toUserDetailsDto(User user) {
        if (user == null) return null;

        return UserDetailsDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(mapUserRolesToStrings(user.getUserRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public List<UserDetailsDto> toUserDetailsDtoList(List<User> users) {
        if (users == null) return Collections.emptyList();
        return users.stream()
                .map(this::toUserDetailsDto)
                .collect(Collectors.toList());
    }

    public UserProfileDto toUserProfileDto(User user) {
        if (user == null) return null;

        return UserProfileDto.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserUpdateResponse toUserUpdateResponse(User user) {
        if (user == null) return null;

        return UserUpdateResponse.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(mapUserRolesToStrings(user.getUserRoles()))
                .updatedAt(String.valueOf(user.getUpdatedAt()))
                .build();
    }

    private Set<String> mapUserRolesToStrings(Set<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptySet();
        }
        return userRoles.stream()
                .filter(ur -> ur.getRole() != null)
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
    }
}
