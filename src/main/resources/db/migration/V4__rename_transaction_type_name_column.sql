-- =================================================================
-- V4: Align column name in budget_transaction_types table
-- =================================================================
ALTER TABLE `budget_transaction_types`
  CHANGE COLUMN `transaction_type_name` `budget_transaction_type_name` VARCHAR(255) NOT NULL;