package com.interviewiq.auth.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        // Jaan-bujh ke generic message: attacker ko pata na chale ki email galat hai ya password.
        super("Invalid email or password");
    }
}
