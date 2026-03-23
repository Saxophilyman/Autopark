package org.example.autopark.appUtil.trackGeneration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Клиент OpenRouteService.
 * Важно: использует RestTemplate из Spring-контекста, значит MonitoringHttpClientInterceptor будет работать автоматически.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRouteServiceClient {

    private final RestTemplate restTemplate;

    // Базовый URL без ключа
    private static final String ORS_DIRECTIONS_URL =
            "https://api.openrouteservice.org/v2/directions/driving-car";

    // В идеале вынести в application.yml, но пока оставим как константу
    private static final String ORS_API_KEY =
            "5b3ce3597851110001cf6248c3876f54a9c846e7bba824abc9f891f3";

    /**
     * Получить маршрут между двумя точками.
     * Возвращает список координат маршрута, либо пустой список при ошибке/пустом ответе.
     */
    public List<GpsPointCoord> getRoute(double startLong, double startLat, double endLong, double endLat) {
        String uri = UriComponentsBuilder.fromHttpUrl(ORS_DIRECTIONS_URL)
                .queryParam("start", startLong + "," + startLat)
                .queryParam("end", endLong + "," + endLat)
                .build(true)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, ORS_API_KEY);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                return List.of();
            }

            return parseRoutePoints(body);

        } catch (HttpStatusCodeException ex) {
            // 4xx/5xx уже будут учтены интерцептором метрик.
            log.warn("OpenRouteService HTTP {}", ex.getStatusCode().value());
            return List.of();
        } catch (Exception ex) {
            // Сетевые ошибки, таймауты и т.п. (интерцептор 4xx/5xx этого не увидит)
            log.warn("OpenRouteService call failed: {}", ex.toString());
            return List.of();
        }
    }

    /**
     * Парсит JSON-ответ ORS и достает координаты маршрута.
     * Логика перенесена из TrackGenService
     */
    private static List<GpsPointCoord> parseRoutePoints(String jsonResponse) {
        List<GpsPointCoord> points = new ArrayList<>();
        try {
            JSONObject responseObject = new JSONObject(jsonResponse);

            if (responseObject.has("error")) {
                // Можно залогировать message, но без излишнего спама
                return points;
            }

            if (!responseObject.has("features")) {
                return points;
            }

            JSONArray features = responseObject.getJSONArray("features");
            if (features.isEmpty()) {
                return points;
            }

            JSONObject firstFeature = features.getJSONObject(0);
            if (!firstFeature.has("geometry")) {
                return points;
            }

            JSONObject geometry = firstFeature.getJSONObject("geometry");
            if (!geometry.has("coordinates")) {
                return points;
            }

            JSONArray coordinates = geometry.getJSONArray("coordinates");

            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray point = coordinates.getJSONArray(i);
                double longitude = point.getDouble(0);
                double latitude = point.getDouble(1);
                points.add(new GpsPointCoord(latitude, longitude));
            }

        } catch (JSONException e) {
            return points;
        }

        return points;
    }
}
