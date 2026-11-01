package com.interviewiq.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// UserDetailsServiceAutoConfiguration exclude kiya hai kyunki hum apni users table use kar rahe hain,
// Spring ka default "generated password" user nahi chahiye.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
