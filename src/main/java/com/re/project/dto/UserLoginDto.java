package com.re.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserLoginDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Set<String> roles;
    private LocalDateTime lastLogin;
}
