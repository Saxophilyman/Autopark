INSERT INTO manager (manager_id, username, password, role)
VALUES
    (1, 'm1', '$2a$10$Y4M803BKBYtswzjQH0F5A.7jgUTSYn/P5Ifoz/ax7BZdLTlK1e10e', 'MANAGER'),
    (2, 'm2', '$2a$10$ohQPZU9M9WRqvwDQmiHo2seDT2J81mVJELBMjd38csXwuXw0DDCQQe', 'MANAGER')
ON CONFLICT (manager_id) DO NOTHING;