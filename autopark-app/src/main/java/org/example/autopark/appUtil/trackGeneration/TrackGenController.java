package org.example.autopark.appUtil.trackGeneration;

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
@RequestMapping("/api/generate")
@Tag(
        name = "Track generation",
        description = "Служебный API для генерации тестовых GPS-треков и поездок"
)
public class TrackGenController {
    private final TrackGenService trackGenService;

    @Autowired
    public TrackGenController(TrackGenService trackGenService) {
        this.trackGenService = trackGenService;
    }

    /**
     * Генерация трека и поездки для автомобиля.
     * Строит маршрут по дорогам через OpenRouteService,
     * сохраняет GPS-точки и создаёт поездку с началом/концом.
     */
    @PostMapping("/track")
    @Operation(
            summary = "Сгенерировать тестовый трек для автомобиля",
            description = """
                    Генерирует GPS-трек для указанного автомобиля:
                    • случайно выбирает стартовую точку около центра,
                    • строит маршрут с заданной длиной,
                    • сохраняет GPS-точки каждые 10 секунд,
                    • создаёт поездку (Trip) с начальной и конечной датой.
                    """
    )
    public ResponseEntity<Void> generateTrack(
            @Parameter(hidden = true)
            @CurrentManagerId Long managerId,

            @RequestBody @Valid TrackGenDTO request,
            BindingResult bindingResult
    ) {
        ValidationBindingUtil.Binding(bindingResult);
        trackGenService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
