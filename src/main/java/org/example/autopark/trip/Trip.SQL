SELECT g.*
FROM gps_points g
         JOIN trips t ON g.vehicle_id_for_gps = t.vehicle_id
WHERE t.start_time >= :startTime
  AND t.end_time <= :endTime
  AND g.timestamp BETWEEN t.start_time AND t.end_time;

-- Что делает этот запрос?
-- Соединяет gps_points с trips по vehicle_id.
-- Отбирает только поездки, которые полностью попадают в запрашиваемый интервал.
-- Возвращает только точки, попадающие в этот диапазон.


CREATE INDEX idx_trip_time ON trip (car_id, start_time, end_time);
-- @Query("""
--     SELECT g FROM GpsPoint g
--     WHERE g.car.id = :carId
--     AND EXISTS (
--         SELECT 1 FROM Trip t
--         WHERE t.car.id = g.car.id
--         AND t.startTime <= g.timestamp
--         AND t.endTime >= g.timestamp
--         AND t.id IN :trips
--     )
-- """)
-- List<GpsPoint> findPointsByTripsAndCar(List<Trip> trips, Long carId);
