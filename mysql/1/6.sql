-- DS QUESTION:
-- Which vehicles have affected the most victims (killed + injured)?

SELECT * FROM parties LIMIT 10;

CREATE OR REPLACE VIEW parties_vehicles_num_victims_cleaned AS (
SELECT
	t1.case_id,
	party_number,
	statewide_vehicle_type,
	vehicle_make,
    killed_victims + injured_victims AS num_affected_victims
FROM parties t1
LEFT JOIN collisions t2 ON t1.case_id = t2.case_id
WHERE at_fault = 1 AND vehicle_year > 0 AND vehicle_make != '' AND statewide_vehicle_type != '');

SELECT * FROM parties_vehicles_num_victims_cleaned;

-- Q1
-- Rank each vehicle make by its risk score
-- Make a grouped table with three columns:
-- 1. vehicle_make
-- 2. total_num_affected_victims (SUM of number of victims affected)
-- 3. risk_score_rank (Desceding rank by total_num_affected_victims)
SELECT
	vehicle_make, total_num_affected_victims
	RANK() OVER (ORDER BY total_num_affected_victims DESC) AS risk_score_rank
FROM 
	(SELECT
		vehicle_make,
		sum(num_affected_victims) as total_num_affected_victims
	FROM parties_vehicles_num_victims_cleaned
	GROUP BY vehicle_make) IQ



-- Q2
-- Rank each vehicle type and make combination by its risk score
-- Make a grouped table with four columns:
-- 1. statewide_vehicle_type
-- 2. vehicle_make
-- 3. total_num_affected_victims (SUM of number of victims affected)
-- 4. risk_score_rank (Desceding rank by total_num_affected_victims within statewide_vehicle_type partition)



SELECT
	statewide_vehicle_type, vehicle_make, total_num_affected_victims
	RANK() OVER (PARTITION BY statewide_vehicle_type ORDER BY total_num_affected_victims DESC) AS risk_score_rank
FROM 
	(SELECT
		statewide_vehicle_type,
		vehicle_make,
		sum(num_affected_victims) as total_num_affected_victims
	FROM parties_vehicles_num_victims_cleaned
	GROUP BY vehicle_make, statewide_vehicle_type) IQ

