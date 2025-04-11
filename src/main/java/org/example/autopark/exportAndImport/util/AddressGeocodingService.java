package org.example.autopark.exportAndImport.util;

import lombok.extern.slf4j.Slf4j;
import org.example.autopark.appUtil.trackGeneration.GpsPointCoord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class AddressGeocodingService {

    public GpsPointCoord geocode(String address) {
        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encoded;

            HttpGet request = new HttpGet(url);
            request.addHeader("User-Agent", "AutoparkApp/1.0 (support@example.com)");

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(request)) {

                String json = EntityUtils.toString(response.getEntity());
                JSONArray arr = new JSONArray(json);

                if (arr.length() > 0) {
                    JSONObject obj = arr.getJSONObject(0);
                    double lat = Double.parseDouble(obj.getString("lat"));
                    double lon = Double.parseDouble(obj.getString("lon"));
                    return new GpsPointCoord(lat, lon);
                } else {
                    log.warn("Геокодер не нашёл координаты для адреса: {}", address);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка геокодирования адреса '{}': {}", address, e.getMessage());
        }

        return null;
    }
}
