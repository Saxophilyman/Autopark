package org.example.autopark.appUtil;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

public class ValidationBindingUtil {
    private ValidationBindingUtil() {
        // Закрываем возможность создания экземпляра класса
    }

//    public static void Binding(BindingResult bindingResult) {
//        if (bindingResult.hasErrors()) {
//            StringBuilder errorMessage = new StringBuilder();
//            bindingResult.getAllErrors().forEach(error ->
//                    errorMessage.append(error.getDefaultMessage()).append("; "));
//            throw new IllegalArgumentException(errorMessage.toString());
//        }
//    }
public static void Binding(BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
        // Собираем все сообщения ошибок в одну строку
        String message = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        // Раньше:
        // throw new IllegalArgumentException(message);

        // Теперь возвращаем клиенту 400 BAD_REQUEST c нашим текстом
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
}
