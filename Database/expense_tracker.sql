CREATE DATABASE expence_tracker;
SHOW databases;
USE expence_tracker;

CREATE TABLE Category(
category_id INT PRIMARY KEY auto_increment,
category_name nvarchar(50) not null unique,
is_default boolean not null default false
);

INSERT INTO Category (category_name,is_default)
VALUES
('Food', TRUE),
('Transport', TRUE),
('Education', TRUE),
('Shopping', TRUE),
('Entertainment', TRUE),
('Bills', TRUE),
('Health', TRUE),
('Other', TRUE);

SELECT * FROM Category;

CREATE TABLE Expense(
expense_id INT PRIMARY KEY auto_increment,
description VARCHAR(255) NOT NULL,
amount DECIMAL(10,2) NOT NULL,
expense_date DATE NOT NULL,
category_id INT NOT NULL,
foreign key(category_id) references Category(category_id));

SELECT * FROM Expense;
DESCRIBE Expense;

INSERT INTO Expense (description, amount,expense_date,category_id)
VALUES
('Lunch', 500.00, '2026-11-02', 1),
('Bus fare', 150.00, '2026-11-02', 2),
('Exercise books', 800.00, '2026-11-01', 3),
('Movie ticket', 1200.00, '2026-11-01', 5),
('Groceries', 2500.00, '2026-11-02', 1);

SELECT
Expense.description,
Expense.amount,
Expense.expense_date,
Category.category_name
from Expense
INNER JOIN Category ON 
Expense.category_id = Category.category_id;

CREATE TABLE Budget (
    budget_id INT PRIMARY KEY AUTO_INCREMENT,
    budget_month DATE NOT NULL UNIQUE,
    budget_amount DECIMAL(10,2) NOT NULL
);
INSERT INTO Budget (budget_month, budget_amount)
VALUES ('2026-11-01', 30000.00);

SELECT
Category.category_name,
Expense.description
from Category left outer join Expense
ON Expense.category_id = Category.category_id;

select Category.category_name,sum(Expense.amount) as 'Total expense'
from Category
left join Expense ON 
Expense.category_id = Category.category_id
GROUP BY Category.category_name;

select Category.category_name,count(Expense.expense_id) as 'Total count'
from Category
inner join Expense ON 
 Category.category_id =Expense.category_id 
GROUP BY Category.category_name;
