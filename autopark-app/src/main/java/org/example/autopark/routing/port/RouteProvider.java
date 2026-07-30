package org.example.autopark.routing.port;



import org.example.autopark.geo.GeoPoint;

import java.util.List;

/**
 * Граница внешнего сервиса построения маршрута.
 *
 * <p>Импорт и генератор зависят от этого контракта, но не друг от друга.</p>
 */
public interface RouteProvider {

    List<GeoPoint> getRoute(
            double startLongitude,
            double startLatitude,
            double endLongitude,
            double endLatitude
    );
}
