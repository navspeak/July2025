-- DS QUESTION:
-- What is the relationship between the age range of the party and the chances of being at fault for the collision?

SELECT * FROM parties LIMIT 10;

-- Solution Approach:
-- Define an age range column using a case statement on the column party_age
-- Filter to party_age != 0
-- Group by the column age_range to find the number of at fault parties and total parties

-- Q1
-- Add a new column age_range in the parties table
-- Use categories 60+, 50-59, 40-49, 30-39, 18-29, <18
-- Filter to party_age != 0

SELECT parties.*,
CASE 
	WHEN party_age >=60 THEN '60+'
    WHEN party_age >50 THEN '50-59'
	WHEN party_age >40 THEN '40-49'
	WHEN party_age >30 THEN '30-39'
	WHEN party_age >=18 THEN '18-29'
    ELSE '<18'
END  as age_group
FROM `parties` where party_age != 0


-- Q2
-- For each of the above age ranges, find out
-- 1) The total num of parties at fault (total_num_at_fault)
-- 2) The total num of parties overall (total_num_parties)
-- 3) fault percentage (fault_percentage = total_num_at_fault / total_num_parties)
with age_group_cte as (
SELECT 
CASE 
	WHEN party_age >60 THEN '60+'
    WHEN party_age >50 THEN '50-59'
	WHEN party_age >40 THEN '40-49'
	WHEN party_age >30 THEN '30-39'
	WHEN party_age >=18 THEN '18-29'
    ELSE '<18'
END  as age_group, parties.* FROM `parties` where party_age != 0)
select age_group, sum(at_fault) total_num_at_fault, count(1) total_num_parties, sum(at_fault)  * 100 / count(1) fault_percentage
from age_group_cte group by age_group order by age_group

