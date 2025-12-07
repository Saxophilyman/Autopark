-- src/test/resources/sql/e2e/insert_vehicles.sql

-- Бренд
INSERT INTO brand (brand_id, brand_name, type, capacity_fuel_tank, load_capacity, number_of_seats)
VALUES
    (1, 'Toyota Camry', 0, 60, 500, 5)
ON CONFLICT (brand_id) DO NOTHING;

-- Машина, которую будем искать в e2e-тесте
INSERT INTO vehicle (
--     vehicle_id,
    vehicle_cost,
    vehicle_name,
    vehicle_year_of_release,
    brand_owner,
    enterprise_owner_of_vehicle,
    purchase_date_utc,
    guid,
    license_plate
)
VALUES
    (
--         1,
        1200000,
        'Camry тестовая',
        2020,
        1,      -- brand_id
        1,      -- enterprise_id (Автоваз)
        '2024-01-01 00:00:00+00',
        gen_random_uuid(),
        'А123ВС'
    )
ON CONFLICT (vehicle_id) DO NOTHING;
