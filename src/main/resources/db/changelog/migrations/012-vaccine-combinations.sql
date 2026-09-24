-- liquibase formatted sql

-- changeset alexandra:create-vaccine-combinations-table
-- comment: Create vaccine combinations mapping table for tracking combination vaccines
CREATE TABLE vaccine_combinations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    combination_vaccine_id BIGINT NOT NULL,
    component_vaccine_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (combination_vaccine_id) REFERENCES vaccines(id) ON DELETE CASCADE,
    FOREIGN KEY (component_vaccine_id) REFERENCES vaccines(id) ON DELETE CASCADE,
    UNIQUE KEY uk_combination_component (combination_vaccine_id, component_vaccine_id)
);

CREATE INDEX idx_combination_vaccine_id ON vaccine_combinations(combination_vaccine_id);
CREATE INDEX idx_component_vaccine_id ON vaccine_combinations(component_vaccine_id);

-- changeset alexandra:add-pentavalent-hexavalent-combinations
-- comment: Add Pentavalent and Hexavalent combination mappings to vaccination schedule
-- First, add Pentavalent and Hexavalent as vaccine options if they don't exist
INSERT IGNORE INTO vaccines (vaccine_name, vaccine_name_gr)
VALUES ('Pentavalent', 'Πενταδύναμο (DTaP-Hib-IPV)');

INSERT IGNORE INTO vaccines (vaccine_name, vaccine_name_gr)
VALUES ('Hexavalent', 'Εξαδύναμο (DTaP-Hib-IPV-HepB)');

-- Map Pentavalent to its components (DTaP, Hib, IPV)
INSERT INTO vaccine_combinations (combination_vaccine_id, component_vaccine_id)
SELECT penta.id, dtap.id FROM vaccines penta, vaccines dtap
WHERE penta.vaccine_name = 'Pentavalent'
AND dtap.vaccine_name LIKE 'DTaP%'
AND NOT EXISTS (
    SELECT 1 FROM vaccine_combinations vc
    WHERE vc.combination_vaccine_id = penta.id AND vc.component_vaccine_id = dtap.id
);

INSERT INTO vaccine_combinations (combination_vaccine_id, component_vaccine_id)
SELECT penta.id, hib.id FROM vaccines penta, vaccines hib
WHERE penta.vaccine_name = 'Pentavalent'
AND hib.vaccine_name LIKE 'Hib%'
AND NOT EXISTS (
    SELECT 1 FROM vaccine_combinations vc
    WHERE vc.combination_vaccine_id = penta.id AND vc.component_vaccine_id = hib.id
);

INSERT INTO vaccine_combinations (combination_vaccine_id, component_vaccine_id)
SELECT penta.id, ipv.id FROM vaccines penta, vaccines ipv
WHERE penta.vaccine_name = 'Pentavalent'
AND ipv.vaccine_name LIKE 'IPV%'
AND NOT EXISTS (
    SELECT 1 FROM vaccine_combinations vc
    WHERE vc.combination_vaccine_id = penta.id AND vc.component_vaccine_id = ipv.id
);

-- Map Hexavalent to its components (DTaP, Hib, IPV, HepB)
INSERT INTO vaccine_combinations (combination_vaccine_id, component_vaccine_id)
SELECT hexa.id, dtap.id FROM vaccines hexa, vaccines dtap
WHERE hexa.vaccine_name = 'Hexavalent'
AND dtap.vaccine_name LIKE 'DTaP%'
AND NOT EXISTS (
    SELECT 1 FROM vaccine_combinations vc
    WHERE vc.combination_vaccine_id = hexa.id AND vc.component_vaccine_id = dtap.id
);

INSERT INTO vaccine_combinations (combination_vaccine_id, component_vaccine_id)
SELECT hexa.id, hib.id FROM vaccines hexa, vaccines hib
WHERE hexa.vaccine_name = 'Hexavalent'
AND hib.vaccine_name LIKE 'Hib%'
AND NOT EXISTS (
    SELECT 1 FROM vaccine_combinations vc
    WHERE vc.combination_vaccine_id = hexa.id AND vc.component_vaccine_id = hib.id
);

INSERT INTO vaccine_combinations (combination_vaccine_id, component_vaccine_id)
SELECT hexa.id, ipv.id FROM vaccines hexa, vaccines ipv
WHERE hexa.vaccine_name = 'Hexavalent'
AND ipv.vaccine_name LIKE 'IPV%'
AND NOT EXISTS (
    SELECT 1 FROM vaccine_combinations vc
    WHERE vc.combination_vaccine_id = hexa.id AND vc.component_vaccine_id = ipv.id
);

INSERT INTO vaccine_combinations (combination_vaccine_id, component_vaccine_id)
SELECT hexa.id, hepb.id FROM vaccines hexa, vaccines hepb
WHERE hexa.vaccine_name = 'Hexavalent'
AND hepb.vaccine_name LIKE 'HepB%'
AND NOT EXISTS (
    SELECT 1 FROM vaccine_combinations vc
    WHERE vc.combination_vaccine_id = hexa.id AND vc.component_vaccine_id = hepb.id
);