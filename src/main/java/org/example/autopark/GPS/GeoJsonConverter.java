package org.example.autopark.GPS;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeoJsonConverter {
    /**
     * Конвертирует список GPS-точек в формат GeoJSON
     *
     * @param points Список GPS-точек
     * @return Map с данными в формате GeoJSON
     */
    public static Map<String, Object> convertToGeoJSON(List<GpsPointDto> points) {
        Map<String, Object> geoJson = new LinkedHashMap<>();
        geoJson.put("type", "FeatureCollection");

        List<Map<String, Object>> features = points.stream().map(point -> {
            Map<String, Object> feature = new LinkedHashMap<>();
            feature.put("type", "Feature");

            Map<String, Object> geometry = new LinkedHashMap<>();
            geometry.put("type", "Point");
            geometry.put("coordinates", Arrays.asList(point.getLongitude(), point.getLatitude()));

            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("timestamp", point.getTimestamp().toString());
            properties.put("vehicleId", point.getVehicleId());

            feature.put("geometry", geometry);
            feature.put("properties", properties);
            return feature;
        }).collect(Collectors.toList());

        geoJson.put("features", features);
        return geoJson;
    }
}
//Возможный вариант со StreamAPI
/// **
// * Конвертация списка точек в GeoJSON
// */
//private Map<String, Object> convertToGeoJSON(List<GpsPoint> points, ZoneId enterpriseTimeZone) {
//    return Map.of(
//            "type", "FeatureCollection",
//            "features", points.stream().map(point -> Map.of(
//                    "type", "Feature",
//                    "geometry", Map.of(
//                            "type", "Point",
//                            "coordinates", List.of(point.getLocation().getX(), point.getLocation().getY())
//                    ),
//                    "properties", Map.of(
//                            "timestamp", convertToEnterpriseTimeZone(point.getTimestamp(), enterpriseTimeZone),
//                            "vehicleId", point.getVehicleGPS().getVehicleId()
//                    )
//            )).collect(Collectors.toList())
//    );
//
//
//}