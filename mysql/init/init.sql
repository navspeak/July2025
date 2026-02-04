DROP DATABASE IF EXISTS sales_info;
CREATE DATABASE sales_info;
USE sales_info;

-- Create the table
CREATE TABLE sales_main (
    sales_id INT,
    sale_date DATE,
    sales INT
);

-- Insert the data
INSERT INTO sales_main (sales_id, sale_date, sales) VALUES
(21, '2022-05-01', 57),
(21, '2022-05-02', 75),
(21, '2022-05-03', 66),
(21, '2022-05-04', 45),
(21, '2022-05-05', 34),
(22, '2022-05-06', 23),
(22, '2022-05-07', 85),
(22, '2022-05-08', 54),
(22, '2022-05-09', 67),
(22, '2022-05-10', 71);

