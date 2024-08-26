insert into enterprise (city, name) VALUES ('Moskow', 'GoodCars');
insert into enterprise (city, name) VALUES ('Smolensk', 'Quiker');
insert into enterprise (city, name) VALUES ('Tula', 'Atom');


INSERT INTO manager (username, password) VALUES ('man1', 'psw1');
INSERT INTO manager (username, password) VALUES ('man2', 'psw2');

INSERT INTO enterprise_manager (enterprise_id, manager_id) VALUES (1, 1);
INSERT INTO enterprise_manager (enterprise_id, manager_id) VALUES (2, 1);
INSERT INTO enterprise_manager (enterprise_id, manager_id) VALUES (2, 2);
INSERT INTO enterprise_manager (enterprise_id, manager_id) VALUES (3, 2);