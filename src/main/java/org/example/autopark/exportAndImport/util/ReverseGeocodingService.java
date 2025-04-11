package org.example.autopark.exportAndImport.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

/*
Возможно стоит вынести в пакет appUtil
Здесь находится для наглядности/удобства
рядом с TripGuidExportMapper
можно отрефакторить и вынести как один отдельный утилитный метод для геокодинга
аналогичный код находится в TripMapper
**/

@Service
public class ReverseGeocodingService {

    private static final String API_KEY = "5b3ce3597851110001cf6248c3876f54a9c846e7bba824abc9f891f3";
    private static final String URL = "https://api.openrouteservice.org/geocode/reverse";

    public String reverseGeocode(Point point) {
        if (point == null) return "Не определено";

        double lon = point.getX();
        double lat = point.getY();

        String requestUrl = URL + "?api_key=" + API_KEY + "&point.lon=" + lon + "&point.lat=" + lat;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(requestUrl);
            request.addHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode == 200 || statusCode == 201) {
                    HttpEntity entity = response.getEntity();
                    String jsonResponse = EntityUtils.toString(entity);
                    return parseResponse(jsonResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Не определено";
    }

    private String parseResponse(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode features = rootNode.path("features");
            if (!features.isEmpty()) {
                return features.get(0).path("properties").path("label").asText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Не определено";
    }
}
