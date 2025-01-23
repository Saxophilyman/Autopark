package org.example.autopark.appUtil;

import org.springframework.validation.BindingResult;

public class ValidationBindingUtil {
    private ValidationBindingUtil() {
        // Закрываем возможность создания экземпляра класса
    }

    public static void Binding(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append("; "));
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }
}
