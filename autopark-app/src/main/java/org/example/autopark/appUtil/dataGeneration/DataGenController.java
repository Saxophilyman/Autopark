package org.example.autopark.appUtil.dataGeneration;

import jakarta.validation.Valid;
import org.example.autopark.appUtil.ValidationBindingUtil;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

// Swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Profile("!reactive")
@RequestMapping("/api/generate/data")
@Tag(
        name = "Data generation",
        description = "Служебный API для генерации тестовых машин и водителей для предприятий"
)
public class DataGenController {

    private final DataGenService dataGenService;

    @Autowired
    public DataGenController(DataGenService dataGenService) {
        this.dataGenService = dataGenService;
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/generate/data
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping
    @Operation(
            summary = "Сгенерировать тестовые данные",
            description = "Генерирует заданное количество машин и водителей для указанных предприятий"
    )
    public ResponseEntity<Void> generateData(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @RequestBody @Valid DataGenDTO request,
            BindingResult bindingResult
    ) {
        // Проверка ошибок валидации DTO
        ValidationBindingUtil.Binding(bindingResult);

        // Пока managerId здесь нужен только для того, чтобы сработал резолвер
        // и мы могли при необходимости добавить проверки доступа
        dataGenService.generate(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
