package org.example.autopark.geo;

/**
 * Нейтральная географическая координата.
 *
 * <p>Тип не принадлежит ни генератору, ни импорту, ни внешнему API и может
 * использоваться на границах геокодирования, маршрутизации и расчётов.</p>
 */
public record GeoPoint(double latitude, double longitude) {

    public double getLat() {
        return latitude;
    }

    public double getLng() {
        return longitude;
    }
}
