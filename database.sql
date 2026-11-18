DROP DATABASE IF EXISTS museum_exhibits;
CREATE DATABASE museum_exhibits CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE museum_exhibits;

CREATE TABLE exhibit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventory_number VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    material VARCHAR(80) NOT NULL,
    material_sensitivity DOUBLE NOT NULL,
    exposure_significance DOUBLE NOT NULL
);

CREATE TABLE inspection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exhibit_id BIGINT NOT NULL,
    inspection_date VARCHAR(10),
    damage_severity DOUBLE NOT NULL,
    FOREIGN KEY (exhibit_id) REFERENCES exhibit(id)
);

CREATE TABLE specialist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(160) NOT NULL,
    specialization VARCHAR(120) NOT NULL
);

CREATE TABLE conservation_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inspection_id BIGINT NOT NULL,
    specialist_id BIGINT NOT NULL,
    state VARCHAR(30) NOT NULL,
    f1 DOUBLE NOT NULL,
    f2 DOUBLE NOT NULL,
    f3 DOUBLE NOT NULL,
    integral_index DOUBLE NOT NULL,
    FOREIGN KEY (inspection_id) REFERENCES inspection(id),
    FOREIGN KEY (specialist_id) REFERENCES specialist(id)
);

INSERT INTO exhibit(inventory_number, name, material, material_sensitivity, exposure_significance) VALUES
('ЕК-001', 'Керамічна ваза', 'кераміка', 40.0, 70.0),
('ЕК-002', 'Лляний рушник', 'льон', 85.0, 60.0);

INSERT INTO inspection(exhibit_id, inspection_date, damage_severity) VALUES
(1, '2026-10-05', 55.0),
(2, '2026-10-12', 30.0);

INSERT INTO specialist(full_name, specialization) VALUES
('Коваленко Ірина Петрівна', 'реставратор кераміки'),
('Мельник Андрій Олегович', 'реставратор текстилю');

-- F1 = exhibit.material_sensitivity, F2 = inspection.damage_severity, F3 = exhibit.exposure_significance
-- I = 0,40*F1 + 0,35*F2 + 0,25*F3
INSERT INTO conservation_request(inspection_id, specialist_id, state, f1, f2, f3, integral_index) VALUES
(1, 1, 'NEW', 40.0, 55.0, 70.0, 52.75),
(2, 2, 'NEW', 85.0, 30.0, 60.0, 59.5);
