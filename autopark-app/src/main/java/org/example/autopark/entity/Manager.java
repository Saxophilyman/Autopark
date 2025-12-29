package org.example.autopark.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Manager")
public class Manager {
    @Id
    @Column(name = "manager_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long managerId;

    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 30, message = "От 2 до 30 символов")
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;

//    @Column(name = "role")
//    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;


    @ManyToMany(mappedBy = "managerList")
    @JsonIgnore
    private List<Enterprise> enterpriseList;

}

