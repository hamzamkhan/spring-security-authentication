package com.hamzamustafakhan.authenticationapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequestDTO {
    @Email(message = "The provided string doesn't follow the email pattern")
    @NotBlank(message = "Email can't be blank")
    private String email;
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
