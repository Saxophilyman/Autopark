package org.example.autopark.trip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.GPS.GpsPointMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class TripMapper {

    private final ModelMapper modelMapper;
    private final GpsPointMapper gpsPointMapper;

    @Autowired
    public TripMapper(ModelMapper modelMapper, GpsPointMapper gpsPointMapper) {
        this.modelMapper = modelMapper;
        this.gpsPointMapper = gpsPointMapper;
    }

//    public TripDTO toDTO(Trip trip, String timeZone, GpsPoint gpsPointStart, GpsPoint gpsPointEnd) {
//        TripDTO tripDTO = modelMapper.map(trip, TripDTO.class);
//
//        if (gpsPointStart != null && gpsPointEnd != null) {
//            tripDTO.setStartLocationInString(reverseGeocodeAddressOfPointGps(gpsPointStart));
//            tripDTO.setEndLocationInString(reverseGeocodeAddressOfPointGps(gpsPointEnd));
//
//            ZoneId zoneId = ZoneId.of(timeZone);
//            ZonedDateTime localTimeStart = ZonedDateTime.ofInstant(gpsPointStart.getTimestamp(), zoneId);
//            ZonedDateTime localTimeEnd = ZonedDateTime.ofInstant(gpsPointEnd.getTimestamp(), zoneId);
//
//            tripDTO.setStartDate(localTimeStart.toLocalDateTime());
//            tripDTO.setEndDate(localTimeEnd.toLocalDateTime());
//        } else {
//            System.out.println("GPS-точки отсутствуют для поездки id=" + trip.getId());
//        }
//
//        return tripDTO;
//    }

    public TripDTO toDTO(Trip trip, String timeZone, GpsPoint gpsPointStart, GpsPoint gpsPointEnd) {
        TripDTO tripDTO = modelMapper.map(trip, TripDTO.class);

        if (gpsPointStart != null) {
            tripDTO.setStartLocationInString(reverseGeocodeAddressOfPointGps(gpsPointStart));
            ZoneId zoneId = ZoneId.of(timeZone);
            ZonedDateTime localTimeStart = ZonedDateTime.ofInstant(gpsPointStart.getTimestamp(), zoneId);
            tripDTO.setStartDate(localTimeStart.toLocalDateTime());
        } else {
            tripDTO.setStartLocationInString("Не определено");
            tripDTO.setStartDate(null);
        }

        if (gpsPointEnd != null) {
            tripDTO.setEndLocationInString(reverseGeocodeAddressOfPointGps(gpsPointEnd));
            ZoneId zoneId = ZoneId.of(timeZone);
            ZonedDateTime localTimeEnd = ZonedDateTime.ofInstant(gpsPointEnd.getTimestamp(), zoneId);
            tripDTO.setEndDate(localTimeEnd.toLocalDateTime());
        } else {
            tripDTO.setEndLocationInString("Не определено");
            tripDTO.setEndDate(null);
        }

        return tripDTO;
    }


    private String reverseGeocodeAddressOfPointGps(GpsPoint gpsPoint) {
        if (gpsPoint == null || gpsPoint.getLocation() == null) return "";

        String addressOfPointGPS = "";
        double longitude = gpsPoint.getLocation().getX();
        double latitude = gpsPoint.getLocation().getY();

        String openRouteUrl = "https://api.openrouteservice.org/geocode/reverse?api_key=5b3ce3597851110001cf6248c3876f54a9c846e7bba824abc9f891f3";
        String requestUrl = openRouteUrl + "&point.lon=" + longitude + "&point.lat=" + latitude;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(requestUrl);
            request.addHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode == 200 || statusCode == 201) {
                    HttpEntity entity = response.getEntity();
                    String jsonResponse = EntityUtils.toString(entity);
                    addressOfPointGPS = parseResponse(jsonResponse);
                } else {
                    System.out.println("Ошибка геокодирования: " + statusCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return addressOfPointGPS;
    }

    private String parseResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            JsonNode featuresNode = rootNode.path("features");
            if (!featuresNode.isEmpty()) {
                JsonNode propertiesNode = featuresNode.get(0).path("properties");
                return propertiesNode.path("label").asText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Адрес не найден";
    }

}