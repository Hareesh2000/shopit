package com.shopit.project.security.payload;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupResponse {
    @NotBlank
    @Size(min = 2, max = 10)
    private String success;

    @NotBlank
    private String message;
}