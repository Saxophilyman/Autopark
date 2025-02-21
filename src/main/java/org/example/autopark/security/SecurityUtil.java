package org.example.autopark.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static Long getAuthenticatedManagerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ManagerDetails managerDetails)) {
            throw new IllegalStateException("Аутентифицированный пользователь не найден!");
        }
        return managerDetails.getManager().getManagerId();
    }
}
