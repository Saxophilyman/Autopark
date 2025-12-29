-- src/test/resources/sql/e2e/clean.sql

TRUNCATE TABLE
    gps_points,
    trips,
    vehicles_drivers,
    driver,
    vehicle,
    enterprise_manager,
    simple_user,
    manager,
    brand,
    enterprise
    RESTART IDENTITY CASCADE;
