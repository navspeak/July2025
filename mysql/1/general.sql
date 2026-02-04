SELECT SUBSTRING_INDEX('450 Fondren, Houston, TX', ' ', 1) AS street_number;
SELECT REGEXP_SUBSTR('450 Fondren, Houston, TX', '^[0-9]+') AS street_number;
select sum(cast(substring_index(address, ' ', 1) as unsigned)) from employee

-- Synatx of window fn
-- <func>(<params>) OVER (PARTITION BY <expr1> ..., ORDER BY <expr2>..., <Frame Definition>,...)
-- Aggregate func => SUM, COUNT, AVG
-- Lead or lag func => LEAD, lag
-- Rank Function => RANK, DENSE_RANK, PERCENT_RANK, ROW_NUMBER
e.g.:
SELECT 
    *, 
    SUM(sales) over (partition by sales_id) as sum_sales_by_id,
    SUM(sales) over (partition by sales_id ORDER BY sales_date) as sum_sales_so_far,
FROM sales_main

-- FRAMES
SELECT 
    *, 
    avg(sales) over (order by sale_date) as avg_sales_so_far
    avg(sales) over (order by sale_date
                            ROWS between 2 PRECEDING and 1 PRECEDING) as avg_sales_2_prev_dates
    avg(sales) over (order by sale_date
                            ROWS between CURRENT_ROW and 1 FOLLOWING) as avg_sales_curr_plus_1_day                       
FROM sales_main

-- ROWS BETWEEN LOWER_BOUND AND UPPER_BOUND
 -- UNBOUNDED PRECEDING/FOLLOWING
 -- N PRECEDING/FOLLOWING

--LEAD or LAG
SELECT 
    *, 
    LEAD(sales) over (order by sale_date) as sales_next_day
    LAG(sales, 2) over (order by sale_date) as sales_2_days_ago
    LAG(sales, 2) over (PARTITION BY sales_id order by sale_date) as sales_2_days_ago_by_sales_id
                     
FROM sales_main

-- LONG & WIDE

SELECT <column_name (generally primary key of wide_tbl)>,
     MAX(CASE WHEN <attribute_col>='<col_wide_1>' THEN <value_col> ELSE null END) AS <col_wide_1>,
     MAX(CASE WHEN <attribute_col>='<col_wide_2>' THEN <value_col> ELSE null END) AS <col_wide_2>,
     MAX(CASE WHEN <attribute_col>='<col_wide_3>' THEN <value_col> ELSE null END) AS <col_wide_3>,
     ..<as many CASE statements as there are columns (other than primary key) in the wide table>..
FROM <long_tbl> 
GROUP BY <column_name (generally primary key of wide_tbl)>;
-- Order of query execution:
    FROM (including JOINs)
    WHERE -> gROUP BY -> HAVING -> WINDOW -> SELECT -> DISTINCR -> ORDER BY -> LIMIT AND OFFSET
