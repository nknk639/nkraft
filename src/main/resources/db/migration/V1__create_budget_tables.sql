-- accounts table
-- -----------------------------------------------------
-- Table `accounts` (口座マスタ)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `accounts` (
  `account_id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_name` VARCHAR(255) NOT NULL,
  `balance` DECIMAL(19, 2) NOT NULL,
  `is_main` TINYINT(1) NOT NULL DEFAULT 0,
  `is_savings` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`account_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `transaction_types` (取引種別マスタ)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `transaction_types` (
  `transaction_type_id` BIGINT NOT NULL AUTO_INCREMENT,
  `transaction_type_name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`transaction_type_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `categories` (カテゴリマスタ)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `categories` (
  `category_id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`category_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `recurring_transactions` (繰り返し予定)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `recurring_transactions` (
  `recurring_transaction_id` BIGINT NOT NULL AUTO_INCREMENT,
  `recurring_transaction_name` VARCHAR(255) NOT NULL,
  `account_id` BIGINT NOT NULL,
  `transaction_type_id` BIGINT NOT NULL,
  `category_id` BIGINT NULL,
  `amount` DECIMAL(19, 2) NOT NULL,
  `memo` TEXT NULL,
  `rule_type` ENUM('毎月', '毎週') NOT NULL,
  `day_of_month` INT NULL,
  `day_of_week` INT NULL,
  PRIMARY KEY (`recurring_transaction_id`),
  INDEX `fk_recurring_transactions_accounts_idx` (`account_id` ASC) VISIBLE,
  INDEX `fk_recurring_transactions_transaction_types_idx` (`transaction_type_id` ASC) VISIBLE,
  INDEX `fk_recurring_transactions_categories_idx` (`category_id` ASC) VISIBLE,
  CONSTRAINT `fk_recurring_transactions_accounts`
    FOREIGN KEY (`account_id`)
    REFERENCES `accounts` (`account_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_recurring_transactions_transaction_types`
    FOREIGN KEY (`transaction_type_id`)
    REFERENCES `transaction_types` (`transaction_type_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_recurring_transactions_categories`
    FOREIGN KEY (`category_id`)
    REFERENCES `categories` (`category_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `borrows` (借入)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `borrows` (
  `borrow_id` BIGINT NOT NULL AUTO_INCREMENT,
  `borrow_name` VARCHAR(255) NOT NULL,
  `total_amount` DECIMAL(19, 2) NOT NULL,
  `repaid_amount` DECIMAL(19, 2) NULL DEFAULT 0.00,
  PRIMARY KEY (`borrow_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `goals` (目標)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `goals` (
  `goal_id` BIGINT NOT NULL AUTO_INCREMENT,
  `goal_name` VARCHAR(255) NOT NULL,
  `target_amount` DECIMAL(19, 2) NOT NULL,
  `saved_amount` DECIMAL(19, 2) NULL DEFAULT 0.00,
  PRIMARY KEY (`goal_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `transactions` (取引)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `transactions` (
  `transaction_id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `transaction_type_id` BIGINT NOT NULL,
  `category_id` BIGINT NULL,
  `transaction_date` DATE NOT NULL,
  `planned_amount` DECIMAL(19, 2) NOT NULL,
  `actual_amount` DECIMAL(19, 2) NULL,
  `transaction_status` ENUM('予定', '完了') NOT NULL DEFAULT '予定',
  `memo` TEXT NULL,
  `recurring_id` BIGINT NULL,
  `borrow_id` BIGINT NULL,
  `goal_id` BIGINT NULL,
  PRIMARY KEY (`transaction_id`),
  INDEX `fk_transactions_accounts_idx` (`account_id` ASC) VISIBLE,
  INDEX `fk_transactions_transaction_types_idx` (`transaction_type_id` ASC) VISIBLE,
  INDEX `fk_transactions_categories_idx` (`category_id` ASC) VISIBLE,
  INDEX `fk_transactions_recurring_transactions_idx` (`recurring_id` ASC) VISIBLE,
  INDEX `fk_transactions_borrows_idx` (`borrow_id` ASC) VISIBLE,
  INDEX `fk_transactions_goals_idx` (`goal_id` ASC) VISIBLE,
  CONSTRAINT `fk_transactions_accounts`
    FOREIGN KEY (`account_id`)
    REFERENCES `accounts` (`account_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transactions_transaction_types`
    FOREIGN KEY (`transaction_type_id`)
    REFERENCES `transaction_types` (`transaction_type_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transactions_categories`
    FOREIGN KEY (`category_id`)
    REFERENCES `categories` (`category_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transactions_recurring_transactions`
    FOREIGN KEY (`recurring_id`)
    REFERENCES `recurring_transactions` (`recurring_transaction_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transactions_borrows`
    FOREIGN KEY (`borrow_id`)
    REFERENCES `borrows` (`borrow_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transactions_goals`
    FOREIGN KEY (`goal_id`)
    REFERENCES `goals` (`goal_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `repayments` (返済履歴)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `repayments` (
  `repayment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `transaction_id` BIGINT NOT NULL,
  `borrow_id` BIGINT NOT NULL,
  PRIMARY KEY (`repayment_id`),
  INDEX `fk_repayments_transactions_idx` (`transaction_id` ASC) VISIBLE,
  INDEX `fk_repayments_borrows_idx` (`borrow_id` ASC) VISIBLE,
  CONSTRAINT `fk_repayments_transactions`
    FOREIGN KEY (`transaction_id`)
    REFERENCES `transactions` (`transaction_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_repayments_borrows`
    FOREIGN KEY (`borrow_id`)
    REFERENCES `borrows` (`borrow_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `goal_achievements` (目標達成履歴)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `goal_achievements` (
  `goal_achievement_id` BIGINT NOT NULL AUTO_INCREMENT,
  `transaction_id` BIGINT NOT NULL,
  `goal_id` BIGINT NOT NULL,
  PRIMARY KEY (`goal_achievement_id`),
  INDEX `fk_goal_achievements_transactions_idx` (`transaction_id` ASC) VISIBLE,
  INDEX `fk_goal_achievements_goals_idx` (`goal_id` ASC) VISIBLE,
  CONSTRAINT `fk_goal_achievements_transactions`
    FOREIGN KEY (`transaction_id`)
    REFERENCES `transactions` (`transaction_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_goal_achievements_goals`
    FOREIGN KEY (`goal_id`)
    REFERENCES `goals` (`goal_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Initial Data
-- -----------------------------------------------------
INSERT INTO `transaction_types` (`transaction_type_name`) VALUES ('出金'), ('入金'), ('振替');