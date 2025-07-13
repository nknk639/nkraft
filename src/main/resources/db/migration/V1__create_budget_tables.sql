-- accounts table
CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL,
    is_main BOOLEAN NOT NULL,
    is_savings BOOLEAN NOT NULL
);

-- transactions table
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    date DATE NOT NULL,
    planned_amount DECIMAL(19, 2) NOT NULL,
    actual_amount DECIMAL(19, 2),
    type ENUM('income', 'expense', 'account_transfer') NOT NULL,
    status ENUM('scheduled', 'completed') NOT NULL,
    category VARCHAR(255),
    memo TEXT,
    recurring_id INT,
    borrow_id INT,
    goal_id INT,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

-- recurring_transactions table
CREATE TABLE recurring_transactions (
    recurring_transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    type ENUM('income', 'expense') NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    day_of_month INT
);

-- borrows table
CREATE TABLE borrows (
    borrow_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    repaid_amount DECIMAL(19, 2)
);

-- repayments table
CREATE TABLE repayments (
    repayment_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    borrow_id INT NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id),
    FOREIGN KEY (borrow_id) REFERENCES borrows(borrow_id)
);

-- goals table
CREATE TABLE goals (
    goal_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    target_amount DECIMAL(19, 2) NOT NULL,
    saved_amount DECIMAL(19, 2)
);

-- goal_achievements table
CREATE TABLE goal_achievements (
    goal_achievement_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    goal_id INT NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id),
    FOREIGN KEY (goal_id) REFERENCES goals(goal_id)
);