package org.example.autopark.simpleUser;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.autopark.entity.Role;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Simple_User")
public class SimpleUser {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int user_id;

    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 30, message = "От 2 до 30 символов")
    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;
}
