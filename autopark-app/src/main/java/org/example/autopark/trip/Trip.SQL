
--для создания колонки интервал (сделано)
ALTER TABLE trips
    ADD COLUMN duration INTERVAL
        GENERATED ALWAYS AS (end_date - start_date) STORED;

 -- Для создания индексов:

-- ускоряет фильтрацию по времени (start_date, end_date) и автомобилю (vehicle_of_trip).
-- Используется в запросах с startTripDate и endTripDate.
CREATE INDEX idx_trips_time ON trips (vehicle_of_trip, start_date, end_date);

-- ускоряет поиск точек по времени (timestamp) и автомобилю (vehicle_id_for_gps).
-- Используется в BETWEEN :startTripDate AND :endTripDate.
CREATE INDEX idx_gps_time ON gps_points (vehicle_id_for_gps, timestamp);

-- только если у нас много запросов с OVERLAPS
CREATE INDEX idx_trips_range ON trips USING GIST (tstzrange(start_date, end_date, '[]'));




-- если ты работаешь с географическими запросами (например, ST_Distance(), ST_Within()).
-- !!!ПОКА НЕ НУЖЕН!!!
CREATE INDEX idx_gps_location ON gps_points USING gist (location);

