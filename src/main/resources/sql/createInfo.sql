INSERT INTO Brand (brand_name, type, capacity_fuel_tank, load_capacity, number_of_seats) VALUES ('Lada', 2, 50, 400, 4);
INSERT INTO Brand (brand_name, type, capacity_fuel_tank, load_capacity, number_of_seats)  VALUES ('Toyota', 2, 65, 480, 4);
INSERT INTO Brand (brand_name, type, capacity_fuel_tank, load_capacity, number_of_seats)  VALUES ('BMW', 2, 80, 600, 4);

INSERT INTO Enterprise (city, name)  VALUES ('Москва','Автоваз');
INSERT INTO Enterprise (city, name) VALUES ('Мюнхен','BMW AG');
INSERT INTO Enterprise (city, name) VALUES ('Тоёта', 'Toyota Motor Corporation');

INSERT INTO Vehicle(vehicle_name, vehicle_cost, vehicle_year_of_release, brand_owner, enterprise_owner_of_vehicle) VALUES
    ('Kalina 2', 1000000, 2013, 1, 1);
INSERT INTO Vehicle(vehicle_name, vehicle_cost, vehicle_year_of_release, brand_owner, enterprise_owner_of_vehicle) VALUES
    ('Granta', 900000, 2011, 1, 1);
INSERT INTO Vehicle(vehicle_name, vehicle_cost, vehicle_year_of_release, brand_owner, enterprise_owner_of_vehicle) VALUES
    ('XRAY', 1500000, 2011, 1, 1);
INSERT INTO Vehicle(vehicle_name, vehicle_cost, vehicle_year_of_release, brand_owner, enterprise_owner_of_vehicle) VALUES
    ('X 7', 9999999, 2017, 3, 2);
INSERT INTO Vehicle(vehicle_name, vehicle_cost, vehicle_year_of_release, brand_owner, enterprise_owner_of_vehicle) VALUES
    ('Camry', 850000, 2014, 2, 3);
INSERT INTO Vehicle(vehicle_name, vehicle_cost, vehicle_year_of_release, brand_owner, enterprise_owner_of_vehicle) VALUES
    ('RAV4', 2000000, 2016, 2, 3);

INSERT INTO Driver (driver_name, driver_salary, enterprise_owner_of_driver, active_vehicle) VALUES ('Попов В.К.', 70000, 1, 1);
INSERT INTO Driver (driver_name, driver_salary, enterprise_owner_of_driver, active_vehicle) VALUES ('Туркин Л.М.', 80000, 1, 2);
INSERT INTO Driver (driver_name, driver_salary, enterprise_owner_of_driver, active_vehicle) VALUES ('Логинов И.К.', 85000, 1, 3);
INSERT INTO Driver (driver_name, driver_salary, enterprise_owner_of_driver, active_vehicle) VALUES ('Майоров Н.Т.', 60000, 2, 4);
INSERT INTO Driver (driver_name, driver_salary, enterprise_owner_of_driver, active_vehicle) VALUES ('Ломов В.В.', 90000, 3, 5);
INSERT INTO Driver (driver_name, driver_salary, enterprise_owner_of_driver, active_vehicle, is_active) VALUES ('Зайкин К.Р.', 100000, 3, 6, true);
INSERT INTO Driver (driver_name, driver_salary, enterprise_owner_of_driver, active_vehicle, is_active) VALUES ('Думкин А.П.', 110000, 4, 5, true);

INSERT INTO Manager (username, password) VALUES ('man1', 'psw1');
INSERT INTO Manager (username, password) VALUES ('man2', 'psw2');

INSERT INTO Enterprise_Manager (enterprise_id, manager_id) VALUES (1, 1);
INSERT INTO Enterprise_Manager (enterprise_id, manager_id) VALUES (2, 1);
INSERT INTO Enterprise_Manager (enterprise_id, manager_id) VALUES (2, 2);
INSERT INTO Enterprise_Manager (enterprise_id, manager_id) VALUES (3, 2);

UPDATE  Manager SET role = 'ADMIN' WHERE manager.manager_id IN (1,2);