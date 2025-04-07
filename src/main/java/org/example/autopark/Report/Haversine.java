package org.example.autopark.Report;

import org.example.autopark.appUtil.trackGeneration.GpsPointCoord;

public class Haversine {
    // Радиус Земли в километрах
    private static final double R = 6371.0;

    // Метод для расчёта расстояния между двумя точками GPS
    public static double calculateDistance(GpsPointCoord point1, GpsPointCoord point2) {
        // Преобразуем координаты из градусов в радианы
        double lat1 = Math.toRadians(point1.getLat());
        double lon1 = Math.toRadians(point1.getLng());
        double lat2 = Math.toRadians(point2.getLat());
        double lon2 = Math.toRadians(point2.getLng());

        // Разность между точками
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        // Формула Haversine
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Расстояние в километрах
        return R * c;
    }
}
