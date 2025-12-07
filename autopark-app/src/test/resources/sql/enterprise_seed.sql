INSERT INTO enterprise (enterprise_id, city, name, timezone, guid)
VALUES
    (1, 'Москва', 'Автоваз', 'Europe/Moscow',  'cbf87fa2-16de-4d5f-a498-5c2671056205'),
    (2, 'Мюнхен', 'BMW AG', 'Europe/Berlin',    '8ef08d06-5cf9-470d-b1bc-aea3852750c2'),
    (3, 'Тойота', 'Toyota Motor Corporation', 'Asia/Tokyo', '7a841cf4-115b-4c94-8d6e-752022418db6')
ON CONFLICT (enterprise_id) DO NOTHING;