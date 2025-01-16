package org.example.autopark.entity;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    MANAGER, USER;

    @Override
    public String getAuthority() {
        return name();
    }
}
