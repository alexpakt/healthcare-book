-- liquibase formatted sql

-- changeset alexandra:insert-who-2006-reference-data
-- comment: WHO 2006 child growth standards for height-for-age (males). Key ages for demonstration.

-- Height for age - MALES - Age 12 months
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'HEIGHT', 'MALE', 3, 71.0, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'HEIGHT', 'MALE', 10, 73.3, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'HEIGHT', 'MALE', 25, 75.9, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'HEIGHT', 'MALE', 50, 79.7, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'HEIGHT', 'MALE', 75, 83.4, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'HEIGHT', 'MALE', 90, 86.0, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'HEIGHT', 'MALE', 97, 88.8, 'WHO_2006');

-- Height for age - MALES - Age 24 months
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'HEIGHT', 'MALE', 3, 86.8, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'HEIGHT', 'MALE', 10, 88.2, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'HEIGHT', 'MALE', 25, 91.0, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'HEIGHT', 'MALE', 50, 95.5, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'HEIGHT', 'MALE', 75, 99.8, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'HEIGHT', 'MALE', 90, 102.6, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'HEIGHT', 'MALE', 97, 104.6, 'WHO_2006');
