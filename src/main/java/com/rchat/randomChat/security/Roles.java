package com.rchat.randomChat.security;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Roles {
    ADMIN("ADMIN", 1),
    USER("USER", 0);

    private final String roleName;
    private final int authorityLevel;

    public String getRole(String role) {
        return Roles.valueOf(role).roleName;
    }

    public int getAuthorityLevel(String role) {
        return Roles.valueOf(role).authorityLevel;
    }

    public String toString() {
        return roleName;
    }
}
