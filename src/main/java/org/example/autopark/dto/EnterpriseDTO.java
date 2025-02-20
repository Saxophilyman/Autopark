package org.example.autopark.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseDTO {

    private Long enterpriseId;

    @NotEmpty
    private String name;

    @NotEmpty
    private String cityOfEnterprise;

    private String timeZone; // Значение по умолчанию устанавливается в маппере/сущности
}
