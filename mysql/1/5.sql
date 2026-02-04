-- DS QUESTION:
-- Which violations have increased / decreased over the years?

SELECT * FROM collisions LIMIT 10;

CREATE OR REPLACE VIEW violation_freq_by_year AS
	(SELECT
		pcf_violation_category,
		YEAR(collision_date) AS collision_year,
		COUNT(*) AS freq
	FROM collisions
	WHERE pcf_violation_category != ''
	GROUP BY pcf_violation_category, collision_year);

SELECT * FROM violation_freq_by_year;

-- Solution Approach:
-- Group by violation_category and collision_year, and get the view above
-- Add column freq_prev_year and avg_freq_prev_3_yrs to the above view
-- 1. freq_prev_year    --> For each (category X year) combination, frequency of that violation category in the previous year
-- 2. avg_freq_prev_3_yrs --> For each (category X year) combination, avg frequency of that violation category in the previous 3 years
SELECT
	*,
	LAG(freq) OVER(PARTITION by pcf_violation_category ORDER BY collision_year) freq_prev_year,
	AVG(freq) OVER(PARTITION by pcf_violation_category ORDER BY collision_year
									ROWS BETWEEN 3 PRECEDING and 1 PRECEDING  ) freq_prev_year,
FROM violation_freq_by_year;

-- RANK

SELECT
	RANK() OVER(order by imdb) as default_rank
from movie_main_sorted_by _imdb

-- DENSE RANK - useful in case of same value
-- PERCENT_RANK
-- ROW_NUMBER