package org.example.autopark.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Enterprise")
public class Enterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enterprise_id")
    private Long enterpriseId;

    @Column(name = "name")
    private String name;

    @Column(name = "city")
    private String cityOfEnterprise;

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "Enterprise_Manager",
            joinColumns = @JoinColumn(name = "enterprise_id"),
            inverseJoinColumns = @JoinColumn(name = "manager_id"))
    private List<Manager> managerList;

    @Column(name="timezone", nullable = false)
    private String timeZone = "UTC";

    @PrePersist
    @PreUpdate
    private void ensureTimezone() {
        if (this.timeZone == null || this.timeZone.isBlank()) {
            this.timeZone = "UTC"; // Если нет значения, устанавливаем UTC
        }
    }
}
