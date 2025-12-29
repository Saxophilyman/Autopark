SET REFERENTIAL_INTEGRITY FALSE;

INSERT INTO brand (
    brand_id,
    brand_name,
    type,
    capacity_fuel_tank,
    load_capacity,
    number_of_seats
)
VALUES
    -- предполагаем, что enum TypeVehicle мапится как ORDINAL (0 = CAR),
    (1, 'Toyota Camry', 0, 60, 500, 5);

INSERT INTO vehicle (
    -- vehicle_id не задаём, его создаст identity
    vehicle_cost,
    vehicle_name,
    vehicle_year_of_release,
    brand_owner,
    enterprise_owner_of_vehicle,
    purchase_date_utc,
    guid,
    license_plate
)
VALUES (
           1200000,
           'Camry тестовая',
           2020,
           1,                         -- brand_id
           1,                         -- enterprise_id (Автоваз)
           TIMESTAMP '2024-01-01 00:00:00',
           RANDOM_UUID(),
           'А123ВС'
       );

SET REFERENTIAL_INTEGRITY TRUE;
