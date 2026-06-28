package com.re.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserUpdateResponse {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private Set<String> roles;
    private String updatedAt;
}
