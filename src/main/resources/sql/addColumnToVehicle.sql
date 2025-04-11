ALTER TABLE Vehicle ADD COLUMN license_plate VARCHAR(10) UNIQUE;


DO $$
    DECLARE
        letters TEXT := 'АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ';
        v_id BIGINT;
        first_letter CHAR;
        last_letters TEXT;
        digits TEXT;
        plate TEXT;
        try_count INT;
    BEGIN
        FOR v_id IN SELECT vehicle_id FROM vehicle LOOP
                try_count := 0;
                LOOP
                    first_letter := substr(letters, floor(random() * length(letters) + 1)::int, 1);
                    digits := lpad(floor(random() * 1000)::int::text, 3, '0');
                    last_letters := substr(letters, floor(random() * length(letters) + 1)::int, 1) ||
                                    substr(letters, floor(random() * length(letters) + 1)::int, 1);
                    plate := first_letter || digits || last_letters;

                    -- Проверка на уникальность
                    PERFORM 1 FROM vehicle WHERE license_plate = plate;
                    IF NOT FOUND THEN
                        UPDATE vehicle SET license_plate = plate WHERE vehicle_id = v_id;
                        EXIT;
                    END IF;

                    try_count := try_count + 1;
                    IF try_count > 50 THEN
                        RAISE WARNING 'Не удалось найти уникальный номер для vehicle_id=%', v_id;
                        EXIT;
                    END IF;
                END LOOP;
            END LOOP;
    END $$;




