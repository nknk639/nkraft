-- =================================================================
-- V3: Align table and column names with the updated requirements
-- =================================================================

-- Step 1: Align budget_transaction_types table
-- -----------------------------------------------------------------

-- Drop foreign key constraints pointing to the old transaction_types table
ALTER TABLE `recurring_transactions` DROP FOREIGN KEY `fk_recurring_transactions_transaction_types`;
ALTER TABLE `transactions` DROP FOREIGN KEY `fk_transactions_transaction_types`;

-- Rename the table
RENAME TABLE `transaction_types` TO `budget_transaction_types`;

-- Rename the primary key column
ALTER TABLE `budget_transaction_types`
  CHANGE COLUMN `transaction_type_id` `budget_transaction_type_id` BIGINT NOT NULL AUTO_INCREMENT;

-- Rename the foreign key columns in referencing tables
ALTER TABLE `recurring_transactions`
  CHANGE COLUMN `transaction_type_id` `budget_transaction_type_id` BIGINT NOT NULL;
ALTER TABLE `transactions`
  CHANGE COLUMN `transaction_type_id` `budget_transaction_type_id` BIGINT NOT NULL;

-- Re-create the foreign key constraints with new names
ALTER TABLE `recurring_transactions`
  ADD CONSTRAINT `fk_recurring_transactions_budget_transaction_types`
  FOREIGN KEY (`budget_transaction_type_id`)
  REFERENCES `budget_transaction_types` (`budget_transaction_type_id`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE `transactions`
  ADD CONSTRAINT `fk_transactions_budget_transaction_types`
  FOREIGN KEY (`budget_transaction_type_id`)
  REFERENCES `budget_transaction_types` (`budget_transaction_type_id`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;


-- Step 2: Align users table
-- -----------------------------------------------------------------

-- Drop foreign key constraints pointing to the old users table
ALTER TABLE `accounts` DROP FOREIGN KEY `fk_accounts_users`;
ALTER TABLE `categories` DROP FOREIGN KEY `fk_categories_users`;
ALTER TABLE `transactions` DROP FOREIGN KEY `fk_transactions_users`;
ALTER TABLE `recurring_transactions` DROP FOREIGN KEY `fk_recurring_transactions_users`;
ALTER TABLE `borrows` DROP FOREIGN KEY `fk_borrows_users`;
ALTER TABLE `goals` DROP FOREIGN KEY `fk_goals_users`;

-- Rename the table
RENAME TABLE `users` TO `nkraft_users`;

-- Rename the primary key column
ALTER TABLE `nkraft_users`
  CHANGE COLUMN `user_id` `nkraft_user_id` BIGINT NOT NULL AUTO_INCREMENT;

-- Rename the foreign key columns in referencing tables
ALTER TABLE `accounts` CHANGE COLUMN `user_id` `nkraft_user_id` BIGINT NOT NULL;
ALTER TABLE `categories` CHANGE COLUMN `user_id` `nkraft_user_id` BIGINT NOT NULL;
ALTER TABLE `transactions` CHANGE COLUMN `user_id` `nkraft_user_id` BIGINT NOT NULL;
ALTER TABLE `recurring_transactions` CHANGE COLUMN `user_id` `nkraft_user_id` BIGINT NOT NULL;
ALTER TABLE `borrows` CHANGE COLUMN `user_id` `nkraft_user_id` BIGINT NOT NULL;
ALTER TABLE `goals` CHANGE COLUMN `user_id` `nkraft_user_id` BIGINT NOT NULL;

-- Re-create the foreign key constraints with new names
ALTER TABLE `accounts`
  ADD CONSTRAINT `fk_accounts_nkraft_users`
  FOREIGN KEY (`nkraft_user_id`)
  REFERENCES `nkraft_users` (`nkraft_user_id`)
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `categories`
  ADD CONSTRAINT `fk_categories_nkraft_users`
  FOREIGN KEY (`nkraft_user_id`)
  REFERENCES `nkraft_users` (`nkraft_user_id`)
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `transactions`
  ADD CONSTRAINT `fk_transactions_nkraft_users`
  FOREIGN KEY (`nkraft_user_id`)
  REFERENCES `nkraft_users` (`nkraft_user_id`)
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `recurring_transactions`
  ADD CONSTRAINT `fk_recurring_transactions_nkraft_users`
  FOREIGN KEY (`nkraft_user_id`)
  REFERENCES `nkraft_users` (`nkraft_user_id`)
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `borrows`
  ADD CONSTRAINT `fk_borrows_nkraft_users`
  FOREIGN KEY (`nkraft_user_id`)
  REFERENCES `nkraft_users` (`nkraft_user_id`)
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `goals`
  ADD CONSTRAINT `fk_goals_nkraft_users`
  FOREIGN KEY (`nkraft_user_id`)
  REFERENCES `nkraft_users` (`nkraft_user_id`)
  ON DELETE CASCADE ON UPDATE CASCADE;