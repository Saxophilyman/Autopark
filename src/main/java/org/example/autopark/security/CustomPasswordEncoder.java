package org.example.autopark.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomPasswordEncoder implements PasswordEncoder {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Сначала проверяем, выглядит ли пароль как зашифрованный BCrypt
        if (encodedPassword.startsWith("$2a$")) {
            return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
        } else {
            // Для незашифрованных паролей (временно)
            return rawPassword.toString().equals(encodedPassword);
        }
    }
}
