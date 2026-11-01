package com.interviewiq.auth.dto;

import com.interviewiq.auth.entity.Role;
import com.interviewiq.auth.entity.User;

public record UserResponse(Long id, String name, String email, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
