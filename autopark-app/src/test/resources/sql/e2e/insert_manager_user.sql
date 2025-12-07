-- src/test/resources/sql/e2e/insert_manager_user.sql

-- Предприятия
INSERT INTO enterprise (enterprise_id, city, name, timezone, guid)
VALUES
    (1, 'Москва',  'Автоваз',                     'Europe/Moscow', gen_random_uuid()),
    (2, 'Мюнхен',  'BMW AG',                      'Europe/Berlin', gen_random_uuid()),
    (3, 'Тойота',  'Toyota Motor Corporation',    'Asia/Tokyo',    gen_random_uuid())
ON CONFLICT (enterprise_id) DO NOTHING;

-- Менеджер для e2e
-- username: m1
-- password: password  (BCrypt, cost=10)
INSERT INTO manager (manager_id, username, password, role)
VALUES
    (1, 'm1', '$2b$10$bh1zQvYIbZITtlZgaaKYqe1HVKJ0xE6AWOItoHtq5FM0BVdu5aM1O', 'MANAGER')
ON CONFLICT (manager_id) DO NOTHING;

-- Связь менеджера с предприятиями (можно сократить, если нужно только одно)
INSERT INTO enterprise_manager (enterprise_id, manager_id)
VALUES
    (1, 1),
    (2, 1),
    (3, 1)
ON CONFLICT DO NOTHING;
