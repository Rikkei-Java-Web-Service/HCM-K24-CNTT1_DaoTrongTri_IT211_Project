package com.re.project.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role; // "CANDIDATE" or "EMPLOYER"
}
