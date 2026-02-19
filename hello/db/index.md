# 🌳 1. Clustered Index (B+ Tree)
👉 In a clustered index, the leaf nodes = actual data rows
* So the B-tree is the table.
```
Table: Orders
Columns: order_id (PK), name, amount

Rows:
(1, "Nav", 100)
(2, "Ved", 200)
(3, "Shlok", 300)
(4, "Sunil", 150)
(5, "Rashi", 500)
```
### Clustered Index B-Tree (on Order-Id)
```
                    [ 3 ]
                  /       \
           [1,2]             [4,5]
         /   |   \         /   |   \
[Row1][Row2][Row3]  [Row4][Row5]
```

### 🔍 What is stored where?
- At Root/ internal nodes => only keys (order_id)
- Leaf Nodes => **full row data**

# 🌳 2. Non-Clustered Index (B+ Tree)
- `CREATE INDEX idx_name ON Orders(name)`
- This creates a separate B-tree
```
                [ "Nav" ]
              /          \
     ["Sunil","Nav"]   ["Shlok","Ved"]
        /     \           /      \
     ptr4   ptr1       ptr3     ptr2

```
- But the leaf does NOT contain the full row. It stores `(name, pointer_to_row)`
  
  | Table type                   | Pointer stored                  |
  | - | - |
  | If table has clustered index | 👉 **clustered key** (order_id) |
  | If heap table                | 👉 physical RID (page + slot)   |
```
Leaf node:
("Nav", order_id=1)
("Ved", order_id=2)
("Shlok", order_id=3)
```
### Query Flow
SELECT * FROM Orders WHERE name = 'Nav';
```html
Step 1: Traverse NON-CLUSTERED index tree
         ↓
        find ("Nav", order_id=1)

Step 2: Use order_id=1 to lookup in CLUSTERED index
         ↓
        fetch actual row
So 2 hops happen.
```

# 🔥 Key Difference Visual
### Clustered Index
- BTree → leaf = actual rows
```
[1] -> (1, Nav, 100)
[2] -> (2, Ved, 200)
```
### Non-Clustered Index
- BTree → leaf = pointer to row
```
"Nav" -> order_id=1
"Ved" -> order_id=2
```
- 📘 Clustered Index = the book itself (pages in order)
- 📑 Non-clustered index = index at back of book

# 1. What is a Bitmap Index (recap in 1 line)
- 👉 A bitmap index stores one bit-array per distinct value
- 👉 Each bit position = one row in the table
- 👉 Good for low cardinality
```html
Table: Customers
Columns: id, name, country

Rows:
1  Navneet   India
2  Ved       Canada
3  Shlok     India
4  Rashi     USA
5  Sunil     India
6  Aryan     Canada

We have 3 distinct countries: India, Canada, USA

CREATE BITMAP INDEX idx_country ON Customers(country);
Row Position:   1  2  3  4  5  6
--------------------------------
India        →  1  0  1  0  1  0
Canada       →  0  1  0  0  0  1
USA          →  0  0  0  1  0  0
Each column is a bitmap
```
### Query Flow
- `SELECT * FROM Customers WHERE country = 'India';`
  - Read bitmap for India =>`1 0 1 0 1 0` => fetch rows at positions 1, 3, 5
  - very fast. No tree traversal 
  - Don't compare Btree : O(log N) vs Bitmap: O(N)
    - In databases where I/O, CPU vectorization, and compression matter much more than raw algorithmic complexity.
  - `SELECT * FROM Customers WHERE country = 'India' OR country = 'Canada';`
    - Engine does bitwise OR
    - No scanning rows — just bit operations.
    ```
      India   : 1 0 1 0 1 0
      Canada  : 0 1 0 0 0 1
      ----------------------
      Result  : 1 1 1 0 1 1
    ```

# Range partitioning
- You split rows into partitions based on ordered ranges of a key (most common: date).
- Example idea: `p_2024 = created_dt < 2025-01-01` `p_2025 = created_dt < 2026-01-01`
- Best for: Time-series / append-heavy tables (events, orders, logs)
- Easy archival: drop old partitions quickly
- Queries like WHERE created_dt BETWEEN ... get partition pruning
- Downsides:
  - If most queries don’t filter by the range key, pruning won’t help
  - You must manage boundaries over time (add future partitions)
-Oracle example
```sql
CREATE TABLE orders (
  order_id     NUMBER,
  created_dt   DATE NOT NULL,
  amount       NUMBER
)
PARTITION BY RANGE (created_dt) (
  PARTITION p2024 VALUES LESS THAN (DATE '2025-01-01'),
  PARTITION p2025 VALUES LESS THAN (DATE '2026-01-01'),
  PARTITION pmax  VALUES LESS THAN (MAXVALUE)
);
```
# List Partitioning
- Best for: “Known set” categories
- Tenant separation (when tenant list is stable)
- Queries filter by those exact values
- Downsides: You must decide where new values go (or inserts can fail if no default partition) and 
Can become messy if the value set grows a lot
```html
CREATE TABLE customers (
id       NUMBER,
country  VARCHAR2(2) NOT NULL,
name     VARCHAR2(100)
)
PARTITION BY LIST (country) (
    PARTITION p_na VALUES ('US','CA'),
    PARTITION p_asia VALUES ('IN','SG'),
    PARTITION p_other VALUES (DEFAULT)
);
```

# Hash partitioning
- You split rows by hash(partition_key) into N roughly-even partitions.
- Example idea: hash(customer_id) % 8 → partition 0..7
- Best for: Even distribution (avoid hotspots), High-throughput OLTP, parallelism, When queries often filter by equality on the key (e.g., customer_id = ?)
- Downsides: Poor for time-based maintenance (can’t “drop last month” easily), Not naturally aligned to range queries (date ranges will touch many partitions)


```sql
--Oracle example
CREATE TABLE events (
id         NUMBER,
customer_id NUMBER NOT NULL,
created_dt  DATE
)
PARTITION BY HASH (customer_id)
PARTITIONS 8;

-- PostgreSQL example
CREATE TABLE events (
id int,
customer_id int,
created_dt date
) PARTITION BY HASH (customer_id);

CREATE TABLE events_p0 PARTITION OF events
FOR VALUES WITH (MODULUS 8, REMAINDER 0);
-- ... p1..p7
```
### Quick “when to use which” rule of thumb
- RANGE: time-based data + archival + range filters
- LIST: known categories / tenants / regions
- HASH: even spread + scalability + avoid hotspots
- Bonus: Composite partitioning (very common): You can combine them, e.g.: RANGE(created_dt) then HASH(customer_id) inside each range
  - This gives: time-based pruning + even distribution within each time slice.

