package com.shopit.project.exceptions;

import java.io.Serial;

public class AuthenticationException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
