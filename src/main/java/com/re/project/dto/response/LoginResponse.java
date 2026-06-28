package com.re.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.re.project.dto.UserLoginDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoginResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;

    @JsonProperty("user")
    private UserLoginDto user;
}
