INSERT INTO enterprise_manager (enterprise_id, manager_id)
VALUES
    (2, 1),
    (2, 2),
    (3, 2),
    (1, 1)
ON CONFLICT DO NOTHING;