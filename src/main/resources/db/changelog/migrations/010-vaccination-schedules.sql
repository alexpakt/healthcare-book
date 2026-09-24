-- liquibase formatted sql

-- changeset alexandra:create-vaccination-schedules-table
-- comment: Create vaccination_schedules table for Greece national vaccination program
CREATE TABLE vaccination_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vaccine_name VARCHAR(100) NOT NULL,
    vaccine_name_gr VARCHAR(255),
    recommended_age_months INT NOT NULL,
    dose_number INT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vaccination_schedule_vaccine_age ON vaccination_schedules(vaccine_name, recommended_age_months);
CREATE INDEX idx_vaccination_schedule_vaccine ON vaccination_schedules(vaccine_name);

-- changeset alexandra:insert-greece-vaccination-schedule-2026
-- comment: Seed Greece national vaccination program schedule for 2026

-- HepB (Hepatitis B)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('HepB (Hepatitis B)', 'Ηπατίτιδας Β', 2, 1, 'Hexavalent - Dose 1 at 2 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('HepB (Hepatitis B)', 'Ηπατίτιδας Β', 4, 2, 'Hexavalent - Dose 2 at 4 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('HepB (Hepatitis B)', 'Ηπατίτιδας Β', 6, 3, 'Hexavalent - Dose 3 at 6 months (total 3-4 doses)');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('HepB (Hepatitis B)', 'Ηπατίτιδας Β', 132, 4, 'Catch-up dose at 11-12 years');

-- DTaP (Diphtheria, Tetanus, Pertussis)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('DTaP (Diphtheria, Tetanus, Pertussis)', 'Διφθερίτιδας, Τετάνου, Κοκκύτη ακυτταρικό', 2, 1, 'Pentavalent/Hexavalent - Dose 1 at 2 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('DTaP (Diphtheria, Tetanus, Pertussis)', 'Διφθερίτιδας, Τετάνου, Κοκκύτη ακυτταρικό', 4, 2, 'Pentavalent/Hexavalent - Dose 2 at 4 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('DTaP (Diphtheria, Tetanus, Pertussis)', 'Διφθερίτιδας, Τετάνου, Κοκκύτη ακυτταρικό', 6, 3, 'Pentavalent/Hexavalent - Dose 3 at 6 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('DTaP (Diphtheria, Tetanus, Pertussis)', 'Διφθερίτιδας, Τετάνου, Κοκκύτη ακυτταρικό', 12, 4, 'Booster at 12 months');

-- Tdap (Tetanus, diphtheria, Pertussis)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Tdap', 'Διφθερίτιδας, Τετάνου, Κοκκύτη', 60, 5, 'Booster at 4-6 years');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Tdap', 'Διφθερίτιδας, Τετάνου, Κοκκύτη', 132, 6, 'Tdap or Tdap-IPV at 11-12 years');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Tdap', 'Διφθερίτιδας, Τετάνου, Κοκκύτη', 198, 7, 'Tdap or Tdap-IPV at 15-18 years');

-- BCG (Bacillus Calmette-Guerin)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('BCG', 'Φυματίωσης (BCG)', 60, 1, 'Dose 1 at 4-6 years for non-vaccinated children');

-- IPV (Inactivated Polio Vaccine)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('IPV (Inactivated Polio Vaccine)', 'Πολιομυελίτιδας αδρανοποιημένο', 2, 1, 'Pentavalent/Hexavalent - Dose 1 at 2 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('IPV (Inactivated Polio Vaccine)', 'Πολιομυελίτιδας αδρανοποιημένο', 4, 2, 'Pentavalent/Hexavalent - Dose 2 at 4 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('IPV (Inactivated Polio Vaccine)', 'Πολιομυελίτιδας αδρανοποιημένο', 6, 3, 'Pentavalent/Hexavalent - Dose 3 at 6 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('IPV (Inactivated Polio Vaccine)', 'Πολιομυελίτιδας αδρανοποιημένο', 12, 4, 'Booster at 12 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('IPV (Inactivated Polio Vaccine)', 'Πολιομυελίτιδας αδρανοποιημένο', 60, 5, 'Booster at 4-6 years');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('IPV (Inactivated Polio Vaccine)', 'Πολιομυελίτιδας αδρανοποιημένο', 132, 6, 'IPV or Tdap-IPV at 11-12 years');

-- Hib (Haemophilus influenzae type b)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Hib (Haemophilus influenzae type b)', 'Αιμόφιλου ινφλουέντζας τύπου b', 2, 1, 'Pentavalent/Hexavalent - Dose 1 at 2 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Hib (Haemophilus influenzae type b)', 'Αιμόφιλου ινφλουέντζας τύπου b', 4, 2, 'Pentavalent/Hexavalent - Dose 2 at 4 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Hib (Haemophilus influenzae type b)', 'Αιμόφιλου ινφλουέντζας τύπου b', 6, 3, 'Pentavalent/Hexavalent - Dose 3 at 6 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Hib (Haemophilus influenzae type b)', 'Αιμόφιλου ινφλουέντζας τύπου b', 12, 4, 'Booster at 12 months');

-- PCV (Pneumococcal Conjugate Vaccine)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('PCV (Pneumococcal Conjugate Vaccine)', 'Πνευμονιόκοκκου συζευγμένο (PCV20 ή PCV15)', 2, 1, 'Pentavalent/Hexavalent - Dose 1 at 2 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('PCV (Pneumococcal Conjugate Vaccine)', 'Πνευμονιόκοκκου συζευγμένο (PCV20 ή PCV15)', 4, 2, 'Pentavalent/Hexavalent - Dose 2 at 4 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('PCV (Pneumococcal Conjugate Vaccine)', 'Πνευμονιόκοκκου συζευγμένο (PCV20 ή PCV15)', 6, 3, 'Pentavalent/Hexavalent - Dose 3 at 6 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('PCV (Pneumococcal Conjugate Vaccine)', 'Πνευμονιόκοκκου συζευγμένο (PCV20 ή PCV15)', 12, 4, 'Booster at 12 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('PCV (Pneumococcal Conjugate Vaccine)', 'Πνευμονιόκοκκου συζευγμένο (PCV20 ή PCV15)', 60, 5, 'Booster at 4-6 years');

-- MCC (Meningococcal C Conjugate)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MCC (Meningococcal C Conjugate)', 'Μηνιγγιτιδόκοκκου οροομάδας C συζευγμένο', 12, 1, 'Dose 1 at 12 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MCC (Meningococcal C Conjugate)', 'Μηνιγγιτιδόκοκκου οροομάδας C συζευγμένο', 60, 2, 'Booster at 4-6 years');

-- MCV4 (Meningococcal Conjugate ACWY)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MCV4 (Meningococcal ACWY Conjugate)', 'Μηνιγγιτιδόκοκκου οροομάδων A,C,W135,Y συζευγμένο', 156, 1, 'Dose 1 at 13-14 years');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MCV4 (Meningococcal ACWY Conjugate)', 'Μηνιγγιτιδόκοκκου οροομάδων A,C,W135,Y συζευγμένο', 198, 2, 'Booster at 15-18 years');

-- MenB (Meningococcal B)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MenB (Meningococcal B (MenB-4C or MenB-HBp))', 'Μηνιγγιτιδόκοκκου οροομάδας B πρωτεϊνικό', 2, 1, 'Dose 1 at 2 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MenB (Meningococcal B (MenB-4C or MenB-HBp))', 'Μηνιγγιτιδόκοκκου οροομάδας B πρωτεϊνικό', 4, 2, 'Dose 2 at 4 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MenB (Meningococcal B (MenB-4C or MenB-HBp))', 'Μηνιγγιτιδόκοκκου οροομάδας B πρωτεϊνικό', 6, 3, 'Dose 3 at 6 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MenB (Meningococcal B (MenB-4C or MenB-HBp))', 'Μηνιγγιτιδόκοκκου οροομάδας B πρωτεϊνικό', 12, 4, 'Booster at 12 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MenB (Meningococcal B (MenB-4C or MenB-HBp))', 'Μηνιγγιτιδόκοκκου οροομάδας B πρωτεϊνικό', 228, 5, 'Catch-up at 18-19 years');

-- Rotavirus
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Rotavirus (RV1 or RV5)', 'Ροταϊός (RV1 ή RV5)', 2, 1, 'Dose 1 at 2 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Rotavirus (RV1 or RV5)', 'Ροταϊός (RV1 ή RV5)', 4, 2, 'Dose 2 at 4 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Rotavirus (RV1 or RV5)', 'Ροταϊός (RV1 ή RV5)', 6, 3, 'Dose 3 at 6 months (only for RV1)');

-- MMR (Measles, Mumps, Rubella)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MMR (Measles, Mumps, Rubella)', 'Ιλαράς, Παρωτίτιδας, Ερυθράς', 12, 1, 'Dose 1 at 12 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MMR (Measles, Mumps, Rubella)', 'Ιλαράς, Παρωτίτιδας, Ερυθράς', 60, 2, 'Booster at 4-6 years');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('MMR (Measles, Mumps, Rubella)', 'Ιλαράς, Παρωτίτιδας, Ερυθράς', 132, 3, 'Catch-up at 11-12 years');

-- Varicella (Chickenpox)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Varicella', 'Ανεμευλογιάς', 12, 1, 'Dose 1 at 12 months');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Varicella', 'Ανεμευλογιάς', 60, 2, 'Booster at 4-6 years');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('Varicella', 'Ανεμευλογιάς', 132, 3, 'Catch-up at 11-12 years');

-- HepA (Hepatitis A)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('HepA (Hepatitis A)', 'Ηπατίτιδας Α', 132, 1, 'Dose 1 at 11-12 years');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('HepA (Hepatitis A)', 'Ηπατίτιδας Α', 228, 2, 'Dose 2 at 18-19 years');

-- HPV (Human Papillomavirus)
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('HPV (Human Papillomavirus)', 'Ιού ανθρώπινων θηλωμάτων', 156, 1, 'Dose 1 at 13-14 years');
INSERT INTO vaccination_schedules (vaccine_name, vaccine_name_gr, recommended_age_months, dose_number, notes) VALUES ('HPV (Human Papillomavirus)', 'Ιού ανθρώπινων θηλωμάτων', 198, 2, 'Dose 2 at 15-18 years');
