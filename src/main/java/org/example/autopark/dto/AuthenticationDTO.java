package org.example.autopark.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationDTO {
    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 30, message = "От 2 до 30 символов")
    private String username;

    private String password;
}
