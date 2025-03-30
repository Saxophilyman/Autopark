-- Расширение для генерации UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ==============================
-- VEHICLE
-- ==============================

ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS guid UUID DEFAULT gen_random_uuid();

UPDATE vehicle SET guid = gen_random_uuid() WHERE guid IS NULL;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'uc_vehicle_guid'
        ) THEN
            ALTER TABLE vehicle ADD CONSTRAINT uc_vehicle_guid UNIQUE (guid);
        END IF;
    END
$$;

-- ==============================
-- DRIVER
-- ==============================

ALTER TABLE driver ADD COLUMN IF NOT EXISTS guid UUID DEFAULT gen_random_uuid();

UPDATE driver SET guid = gen_random_uuid() WHERE guid IS NULL;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'uc_driver_guid'
        ) THEN
            ALTER TABLE driver ADD CONSTRAINT uc_driver_guid UNIQUE (guid);
        END IF;
    END
$$;

-- ==============================
-- ENTERPRISE
-- ==============================

ALTER TABLE enterprise ADD COLUMN IF NOT EXISTS guid UUID DEFAULT gen_random_uuid();

UPDATE enterprise SET guid = gen_random_uuid() WHERE guid IS NULL;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'uc_enterprise_guid'
        ) THEN
            ALTER TABLE enterprise ADD CONSTRAINT uc_enterprise_guid UNIQUE (guid);
        END IF;
    END
$$;

-- ==============================
-- TRIP
-- ==============================

ALTER TABLE trips ADD COLUMN IF NOT EXISTS guid UUID DEFAULT gen_random_uuid();

UPDATE trips SET guid = gen_random_uuid() WHERE guid IS NULL;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'uc_trip_guid'
        ) THEN
            ALTER TABLE trips ADD CONSTRAINT uc_trip_guid UNIQUE (guid);
        END IF;
    END
$$;

-- ==============================
-- GPSPOINT
-- ==============================

ALTER TABLE gps_points ADD COLUMN IF NOT EXISTS guid UUID DEFAULT gen_random_uuid();

UPDATE gps_points SET guid = gen_random_uuid() WHERE guid IS NULL;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'uc_gps_point_guid'
        ) THEN
            ALTER TABLE gps_points ADD CONSTRAINT uc_gps_point_guid UNIQUE (guid);
        END IF;
    END
$$;
