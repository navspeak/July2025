| Function       | Behavior                                |
|----------------|-----------------------------------------|
| `ROW_NUMBER()` | Always gives unique sequence (1,2,3...) |
| `RANK()`       | Skips ranks if duplicates (1,1,3)       |
| `DENSE_RANK()` | No gaps (1,1,2)                         |

```sql
SELECT DEPT, SALARY AS SECOND_HIGHEST_SALARY
FROM (
    SELECT 
        DEPT,
        SALARY,
        DENSE_RANK() OVER (PARTITION BY DEPT ORDER BY SALARY DESC) AS RN
    FROM EMPLOYEE
) t
WHERE RN = 2;
--using Row Number will give wrong value if there are highest salary is repeated

```