-- добавляем колонку timezone к предприятию
ALTER TABLE Enterprise ADD COLUMN timezone VARCHAR(50) DEFAULT 'UTC' NOT NULL;
-- список таймзон
SELECT * FROM pg_timezone_names;
--
ALTER TABLE Vehicle ADD COLUMN purchase_date_utc TIMESTAMP WITHOUT TIME ZONE DEFAULT now();

