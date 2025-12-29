SET REFERENTIAL_INTEGRITY FALSE;

INSERT INTO enterprise (enterprise_id, city, name, timezone, guid)
VALUES
    (1, 'Москва',  'Автоваз',                  'Europe/Moscow', RANDOM_UUID()),
    (2, 'Мюнхен',  'BMW AG',                   'Europe/Berlin', RANDOM_UUID()),
    (3, 'Тойота',  'Toyota Motor Corporation', 'Asia/Tokyo',    RANDOM_UUID());

INSERT INTO manager (manager_id, username, password, role)
VALUES
    -- username: m1, password: password (тот же bcrypt что и в Postgres)
    (1, 'm1', '$2b$10$bh1zQvYIbZITtlZgaaKYqe1HVKJ0xE6AWOItoHtq5FM0BVdu5aM1O', 'MANAGER');

INSERT INTO enterprise_manager (enterprise_id, manager_id)
VALUES
    (1, 1),
    (2, 1),
    (3, 1);

SET REFERENTIAL_INTEGRITY TRUE;
