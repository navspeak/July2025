-- DS QUESTION:
-- What is the relationship between cellphone usage and age for parties at fault for collisions in Los Angeles?

SELECT * FROM parties LIMIT 10;

SELECT * FROM collisions LIMIT 10;

SELECT case_id, county_location FROM `collisions` WHERE county_location = 'Los Angeles';
select case_id, cellphone_in_use, cellphone_use_type, at_fault, party_age from parties
where case_id in (SELECT case_id FROM `collisions` WHERE county_location = 'Los Angeles')

-- SQL QUESTION:
-- For different categories of cell_phone_use_type, what is the frequency and average age of at fault parties?
-- county_location should be "los angeles"

select cellphone_use_type,  avg(party_age), count(P.case_id) from parties P join collisions C on P.case_id = C.case_id
where county_location = 'Los Angeles' and P.at_fault = 1
group by cellphone_use_type

-- Solution Approach:
-- GROUP BY cellphone_use_type, find average age and freq of each group
-- Filter above result to only include collisions with case_id corresponding to collision in Los Angeles


-- Q1
-- Find the average age of at fault parties

SELECT 
  AVG(party_age) AS avg_party_age
FROM parties
JOIN collisions USING (case_id)
WHERE at_fault = 1 
  AND county_location = 'Los Angeles';

-- Q2
-- For different categories of cell_phone_use_type,
-- find the number of at fault parties and the average age of at fault parties
-- RULE: Every non-aggregated column in the SELECT list must appear in the GROUP BY clause.

SELECT 
    cellphone_use_type,
    count(*) AS num_fault_parties,
    AVG(party_age) AS avg_party_age
FROM parties
WHERE at_fault = 1 
GROUP BY cellphone_use_type
order by cellphone_use_type


-- Q3
-- Do the same analysis as Q2, but only for collisions for which county_location is "los angeles".
-- For different categories of cell_phone_use_type,
-- find the number of at fault parties and the average age of at fault parties
SELECT 
    cellphone_use_type,
    count(*) AS num_fault_parties,
    AVG(party_age) AS avg_party_age
FROM parties join collisions USING(case_id)
WHERE at_fault = 1 and county_location = 'Los Angeles'
GROUP BY cellphone_use_type
order by cellphone_use_type

-- When not to use a join
-- If the columns you’re joining on:
-- don’t have a logical one-to-one or one-to-many relationship, or
-- you’re just filtering and not combining attributes,
-- then a subquery (like below query example) might be more conceptually clear.

or

SELECT 
    cellphone_use_type,
    count(*) AS num_fault_parties,
    AVG(party_age) AS avg_party_age
FROM parties 
WHERE at_fault = 1 and case_id in ( select case_id from collisions where county_location = 'Los Angeles')
GROUP BY cellphone_use_type
order by cellphone_use_type

-- Q3 (With CTE)
with  los_angeles_case_id as (
     select case_id from collisions where county_location = 'Los Angeles'
    )
SELECT 
    cellphone_use_type,
    count(*) AS num_fault_parties,
    AVG(party_age) AS avg_party_age
FROM parties
WHERE at_fault = 1 and case_id in (select * from los_angeles_case_id)
GROUP BY cellphone_use_type
order by cellphone_use_type
