package org.example.autopark.trackingRef.entry.web.generation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.autopark.appUtil.ValidationBindingUtil;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;

import org.example.autopark.trackingRef.application.generation.GenerateTrackCommand;
import org.example.autopark.trackingRef.application.generation.TrackGenService;
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
@RequestMapping("/api/generate")
@RequiredArgsConstructor
@Tag(
        name = "Track generation",
        description = "Служебный API для генерации тестовых GPS-треков и поездок"
)
public class TrackGenController {

    private final TrackGenService trackGenService;

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

        trackGenService.generate(
                new GenerateTrackCommand(
                        request.getIdVehicle(),
                        request.getLengthOfTrack(),
                        request.getDate()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
