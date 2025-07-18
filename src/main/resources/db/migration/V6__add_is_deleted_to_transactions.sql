-- =================================================================
-- V6: Add is_deleted flag for logical deletion of transactions
-- =================================================================
ALTER TABLE `transactions`
  ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 AFTER `goal_id`;