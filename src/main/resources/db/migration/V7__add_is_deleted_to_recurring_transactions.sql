-- =================================================================
-- V7: Add is_deleted flag for logical deletion of recurring_transactions
-- =================================================================
ALTER TABLE `recurring_transactions`
  ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 AFTER `day_of_week`;