-- =================================================================
-- V8: Add is_deleted flag to tables and add system categories
-- =================================================================

-- Add is_deleted flag for logical deletion based on requirements F-B13
ALTER TABLE `accounts`
  ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 AFTER `is_savings`;

ALTER TABLE `categories`
  ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 AFTER `category_name`;

ALTER TABLE `borrows`
  ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 AFTER `repaid_amount`;

ALTER TABLE `goals`
  ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 AFTER `saved_amount`;

-- Add system-related categories for 'natsuki' user (id=2)
INSERT INTO `categories` (`nkraft_user_id`, `category_name`) VALUES
(2, '借入返済'),
(2, '目標貯金');