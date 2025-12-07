package org.example.autopark.gps;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ с треком автомобиля")
public class TrackResponse {

    @Schema(
            description = "Формат данных трека: json или geojson",
            example = "json"
    )
    private String format; // "json" или "geojson"

    @Schema(
            description = """
                    Данные трека:
                    • если format = "json" — список GpsPointDto;
                    • если format = "geojson" — объект GeoJSON FeatureCollection.
                    """)
    private Object data;   // Список точек или GeoJSON
}

/**
 * Обёртка вокруг метода getTrack в GpsPointsService
 *
 * ❌ Минусы использования Object
 * ❌ Потеря явной типизации — клиент не всегда понимает, что именно вернёт метод.
 * ❌ Неудобно для документации API (например, OpenAPI/Swagger), так как нельзя чётко определить, какие типы данных возвращает метод.
 * ❌ Может усложнить сериализацию — например, если Spring Boot не сможет корректно преобразовать объект в JSON.
 * --------------------------------
 * ✔ Стало более типизировано — теперь метод всегда возвращает TrackResponse.
 * ✔ Упрощает работу с API — клиент знает, в каком формате пришли данные.
 * ✔ Подходит для документации (Swagger).
 * ✔ Легко расширяется (можно добавить другие форматы).
 *
 * Использование Object допустимо, но с TrackResponse код читаемее и понятнее
 * -------------
 * Простая поддержка новых форматов
 * Допустим, завтра нам нужно добавить экспорт в CSV. С TrackResponse это просто:
 * if ("csv".equalsIgnoreCase(format)) {
 *     return new TrackResponse(format, convertToCSV(dtoPoints));}
 *
 */