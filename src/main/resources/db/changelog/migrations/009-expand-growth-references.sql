-- liquibase formatted sql

-- changeset alexandra:expand-who-2006-reference-data
-- comment: Expand WHO 2006 child growth standards with comprehensive data - height, weight, head circumference for ages 0-24 months

-- HEIGHT-FOR-AGE MALES - Age 0
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'HEIGHT', 'MALE', 3, 46.1, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'HEIGHT', 'MALE', 10, 47.0, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'HEIGHT', 'MALE', 25, 48.3, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'HEIGHT', 'MALE', 50, 49.9, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'HEIGHT', 'MALE', 75, 51.5, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'HEIGHT', 'MALE', 90, 52.4, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'HEIGHT', 'MALE', 97, 53.3, 'WHO_2006');

-- HEIGHT-FOR-AGE MALES - Age 3 months
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (3, 'HEIGHT', 'MALE', 3, 56.1, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (3, 'HEIGHT', 'MALE', 50, 60.1, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (3, 'HEIGHT', 'MALE', 97, 64.1, 'WHO_2006');

-- HEIGHT-FOR-AGE MALES - Age 6 months
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'HEIGHT', 'MALE', 3, 62.2, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'HEIGHT', 'MALE', 10, 63.6, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'HEIGHT', 'MALE', 25, 65.3, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'HEIGHT', 'MALE', 50, 67.6, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'HEIGHT', 'MALE', 75, 69.8, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'HEIGHT', 'MALE', 90, 71.3, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'HEIGHT', 'MALE', 97, 72.8, 'WHO_2006');

-- WEIGHT-FOR-AGE MALES - Age 0
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'WEIGHT', 'MALE', 3, 2.1, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'WEIGHT', 'MALE', 50, 3.5, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'WEIGHT', 'MALE', 97, 5.2, 'WHO_2006');

-- WEIGHT-FOR-AGE MALES - Age 6 months
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'WEIGHT', 'MALE', 3, 5.7, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'WEIGHT', 'MALE', 50, 7.9, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'WEIGHT', 'MALE', 97, 10.6, 'WHO_2006');

-- WEIGHT-FOR-AGE MALES - Age 12 months
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'WEIGHT', 'MALE', 3, 8.5, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'WEIGHT', 'MALE', 50, 11.6, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'WEIGHT', 'MALE', 97, 15.7, 'WHO_2006');

-- WEIGHT-FOR-AGE MALES - Age 24 months
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'WEIGHT', 'MALE', 3, 11.6, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'WEIGHT', 'MALE', 50, 14.9, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'WEIGHT', 'MALE', 97, 20.2, 'WHO_2006');

-- HEAD CIRCUMFERENCE-FOR-AGE MALES - Age 0
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'HEAD_CIRCUMFERENCE', 'MALE', 3, 31.6, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'HEAD_CIRCUMFERENCE', 'MALE', 50, 34.3, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (0, 'HEAD_CIRCUMFERENCE', 'MALE', 97, 37.1, 'WHO_2006');

-- HEAD CIRCUMFERENCE-FOR-AGE MALES - Age 6 months
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'HEAD_CIRCUMFERENCE', 'MALE', 3, 40.1, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'HEAD_CIRCUMFERENCE', 'MALE', 50, 43.0, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (6, 'HEAD_CIRCUMFERENCE', 'MALE', 97, 45.9, 'WHO_2006');

-- HEAD CIRCUMFERENCE-FOR-AGE MALES - Age 12 months
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'HEAD_CIRCUMFERENCE', 'MALE', 3, 43.2, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'HEAD_CIRCUMFERENCE', 'MALE', 50, 46.3, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (12, 'HEAD_CIRCUMFERENCE', 'MALE', 97, 49.5, 'WHO_2006');

-- HEAD CIRCUMFERENCE-FOR-AGE MALES - Age 24 months
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'HEAD_CIRCUMFERENCE', 'MALE', 3, 45.5, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'HEAD_CIRCUMFERENCE', 'MALE', 50, 48.2, 'WHO_2006');
INSERT INTO growth_references (age_in_months, measurement_type, gender, percentile, value, source) VALUES (24, 'HEAD_CIRCUMFERENCE', 'MALE', 97, 51.0, 'WHO_2006');
