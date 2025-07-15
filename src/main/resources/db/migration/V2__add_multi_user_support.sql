-- =================================================================
-- V2: Add multi-user support
-- =================================================================

-- -----------------------------------------------------
-- Table `users` (ユーザーマスタ)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`user_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Add user_id to existing tables
-- -----------------------------------------------------
ALTER TABLE `accounts`
  ADD COLUMN `user_id` BIGINT NOT NULL AFTER `account_id`,
  ADD INDEX `fk_accounts_users_idx` (`user_id` ASC) VISIBLE,
  ADD CONSTRAINT `fk_accounts_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

ALTER TABLE `categories`
  ADD COLUMN `user_id` BIGINT NOT NULL AFTER `category_id`,
  ADD INDEX `fk_categories_users_idx` (`user_id` ASC) VISIBLE,
  ADD CONSTRAINT `fk_categories_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

ALTER TABLE `transactions`
  ADD COLUMN `user_id` BIGINT NOT NULL AFTER `transaction_id`,
  ADD INDEX `fk_transactions_users_idx` (`user_id` ASC) VISIBLE,
  ADD CONSTRAINT `fk_transactions_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

ALTER TABLE `recurring_transactions`
  ADD COLUMN `user_id` BIGINT NOT NULL AFTER `recurring_transaction_id`,
  ADD INDEX `fk_recurring_transactions_users_idx` (`user_id` ASC) VISIBLE,
  ADD CONSTRAINT `fk_recurring_transactions_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

ALTER TABLE `borrows`
  ADD COLUMN `user_id` BIGINT NOT NULL AFTER `borrow_id`,
  ADD INDEX `fk_borrows_users_idx` (`user_id` ASC) VISIBLE,
  ADD CONSTRAINT `fk_borrows_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

ALTER TABLE `goals`
  ADD COLUMN `user_id` BIGINT NOT NULL AFTER `goal_id`,
  ADD INDEX `fk_goals_users_idx` (`user_id` ASC) VISIBLE,
  ADD CONSTRAINT `fk_goals_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;


-- -----------------------------------------------------
-- Initial User Data
-- -----------------------------------------------------
-- Note: Passwords should be hashed by Spring Security.
-- These are placeholders for development.
INSERT INTO `users` (`username`, `password`) VALUES ('test', 'test');
INSERT INTO `users` (`username`, `password`) VALUES ('natsuki', 'mm5c53um');