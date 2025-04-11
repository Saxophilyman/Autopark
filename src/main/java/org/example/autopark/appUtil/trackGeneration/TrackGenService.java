package org.example.autopark.appUtil.trackGeneration;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.example.autopark.GPS.GenerateRandomTimeForDate;
import org.example.autopark.GPS.GpsPoint;
import org.example.autopark.GPS.GpsPointsService;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.VehicleService;
import org.example.autopark.trip.Trip;
import org.example.autopark.trip.TripService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.aspectj.bridge.Version.getTime;

@Service
@Transactional(readOnly = true)
public class TrackGenService {
    private final VehicleService vehicleService;
    private final GpsPointsService gpsPointsService;
    private final TripService tripService;

    double centerLongitude = 37.614720;
    double centerLatitude = 55.757071;
    double radius = 6; // в км

    private final String openRouteUrl = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248c3876f54a9c846e7bba824abc9f891f3";

    @Autowired
    public TrackGenService(VehicleService vehicleService, GpsPointsService gpsPointsService, TripService tripService) {
        this.vehicleService = vehicleService;
        this.gpsPointsService = gpsPointsService;
        this.tripService = tripService;
    }

    @Transactional
    public void generate(TrackGenDTO request) {

        int lengthOfTrack = request.getLengthOfTrack(); // Длина маршрута
        GpsPointCoord start = generateStart(); // Генерация начальной точки
        GpsPointCoord end = generateEnd(start, lengthOfTrack); // Генерация конечной точки

        //находим автомобиль по id
        Vehicle vehicle = vehicleService.findOne(request.getIdVehicle());

        //получает маршрут между точками.
        List<GpsPointCoord> track = getRouting(
                start.getLng(), start.getLat(),
                end.getLng(), end.getLat()
        );

        List<GpsPoint> points = new ArrayList<>();

        // Генерируем случайное время в пределах указанной даты
        Instant startTimestamp = GenerateRandomTimeForDate.generateRandomTimeForDate(request.getDate());
        Instant currentTimestamp = startTimestamp; // Начальная точка

        for (GpsPointCoord pointOfTrack : track) {
            GpsPoint point = new GpsPoint();
            point.setVehicleIdForGps(vehicle); // Привязываем к авто

            GeometryFactory geometryFactory = new GeometryFactory();
            Coordinate coordinate = new Coordinate(pointOfTrack.getLng(), pointOfTrack.getLat());
            Point coord = geometryFactory.createPoint(coordinate);
            point.setLocation(coord);

            point.setTimestamp(currentTimestamp); // Записываем вычисленное время

            points.add(point);
            gpsPointsService.save(point); // Сохраняем в базу

            // Прибавляем 10 секунд к следующей точке
            currentTimestamp = currentTimestamp.plusSeconds(10);
        }
        System.out.println("Всё В ПОРЯДКЕ");
        Instant timeOfStart = points.get(0).getTimestamp();
        Instant timeOfEnd = points.get(points.size()-1).getTimestamp();
        Trip trip = new Trip(vehicle, timeOfStart, timeOfEnd);
        tripService.save(trip);
    }

    public GpsPointCoord generateStart() {
        Random random = new Random();
        // Генерируем случайный угол от 0 до 2π (360 градусов)
        double angle = 2 * Math.PI * random.nextDouble(); // случайный угол
        // Генерируем случайный радиус, используя квадратный корень (чтобы точки были равномерно распределены)
        double r = radius * Math.sqrt(random.nextDouble()); // случайный радиус

        // Вычисляем долготу и широту
        double longitude = centerLongitude + r * Math.cos(angle) / (111.32 * Math.cos(centerLatitude));
        double latitude = centerLatitude + r * Math.sin(angle) / 111.32;

        return new GpsPointCoord(latitude, longitude);
    }

    public GpsPointCoord generateEnd(GpsPointCoord start, int lengthOfTrack) {
        double degreesPerKm = 0.0089;
        double angle = Math.random() * 2 * Math.PI;

        double deltaLatitude = lengthOfTrack * degreesPerKm * Math.cos(angle);
        double deltaLongitude = lengthOfTrack * degreesPerKm * Math.sin(angle);

        double latitude = start.getLat() + deltaLatitude;
        double longitude = start.getLng() + deltaLongitude;

        return new GpsPointCoord(latitude,longitude);
    }

    public List<GpsPointCoord> getRouting(double startLong, double startLat, double endLong, double endLat) {
        List<GpsPointCoord> points = new ArrayList<>();
        String requestUrl = openRouteUrl + "&start=" + startLong  + "," + startLat + "&end=" + endLong + "," + endLat;
        System.out.println(requestUrl);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(requestUrl);
            request.addHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode(); // Новый метод для получения кода ответа
                System.out.println(statusCode + "Здесь");
                if (statusCode == 200 || statusCode == 201) {
                    HttpEntity entity = response.getEntity();
                    String jsonResponse = EntityUtils.toString(entity);
                    System.out.println("Response: " + jsonResponse);

                    // Парсим ответ и добавляем в points
                    points = parseResponse(jsonResponse);
                    if (points.isEmpty()) {
                        System.out.println("Ошибка: маршрут не содержит точек!");
                    }
                } else {
                    System.out.println("WRONG!!! NON!!!: " + statusCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return points;
    }

    private static List<GpsPointCoord> parseResponse(String jsonResponse) {
        List<GpsPointCoord> points = new ArrayList<>();
        try {
            JSONObject responseObject = new JSONObject(jsonResponse);

            // Проверяем, есть ли ошибка в JSON
            if (responseObject.has("error")) {
                System.out.println("Ошибка API: " + responseObject.getJSONObject("error").getString("message"));
                return points;
            }

            // Проверяем, есть ли "features"
            if (!responseObject.has("features")) {
                System.out.println("Ошибка: JSON-ответ не содержит features!");
                return points;
            }

            JSONArray features = responseObject.getJSONArray("features");
            if (features.isEmpty()) {
                System.out.println("Ошибка: API вернул пустой маршрут.");
                return points;
            }

            // Получаем первую Feature
            JSONObject firstFeature = features.getJSONObject(0);

            // Достаём объект geometry
            if (!firstFeature.has("geometry")) {
                System.out.println("Ошибка: JSON не содержит geometry!");
                return points;
            }

            JSONObject geometry = firstFeature.getJSONObject("geometry");

            // Проверяем наличие coordinates
            if (!geometry.has("coordinates")) {
                System.out.println("Ошибка: JSON не содержит coordinates!");
                return points;
            }

            JSONArray coordinates = geometry.getJSONArray("coordinates");

            // Обрабатываем координаты маршрута
            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray point = coordinates.getJSONArray(i);
                double longitude = point.getDouble(0);
                double latitude = point.getDouble(1);
                points.add(new GpsPointCoord(latitude, longitude)); // lat (Y), lng (X)
            }

            // Получаем расстояние из properties → segments
            JSONObject properties = firstFeature.getJSONObject("properties");
            if (properties.has("segments")) {
                JSONArray segments = properties.getJSONArray("segments");
                if (!segments.isEmpty()) {
                    double distance = segments.getJSONObject(0).getDouble("distance");
                    System.out.println("Длина маршрута: " + distance + " метров");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Ошибка при разборе JSON ответа!");
        }
        return points;
    }




}
