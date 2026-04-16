package com.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record UpdateUserRequest(
    @NotBlank(message = "Email cann't blank")
    @Email(message = "Invalid email Format")
    String email
) {
}