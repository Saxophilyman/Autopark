package org.example.autopark.datageneration.entry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.autopark.appUtil.ValidationBindingUtil;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.datageneration.application.GenerateEnterpriseDataCommand;
import org.example.autopark.datageneration.application.GenerateEnterpriseDataUseCase;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("!reactive")
@RequestMapping("/api/generate/data")
@Tag(
        name = "Data generation",
        description = "Служебный API для наполнения предприятий автомобилями и водителями"
)
public class DataGenerationController {

    private final GenerateEnterpriseDataUseCase generateEnterpriseData;

    public DataGenerationController(GenerateEnterpriseDataUseCase generateEnterpriseData) {
        this.generateEnterpriseData = generateEnterpriseData;
    }

    @PostMapping
    @Operation(
            summary = "Сгенерировать данные предприятия",
            description = "Создаёт заданное количество автомобилей и водителей для указанных предприятий"
    )
    public ResponseEntity<Void> generateData(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @RequestBody @Valid DataGenerationRequest request,
            BindingResult bindingResult
    ) {
        ValidationBindingUtil.Binding(bindingResult);

        generateEnterpriseData.execute(new GenerateEnterpriseDataCommand(
                request.enterpriseIds(),
                request.vehicleCount(),
                request.driverCount(),
                request.assignmentStep()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
