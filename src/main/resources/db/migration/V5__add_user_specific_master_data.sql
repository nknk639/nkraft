-- =================================================================
-- V5: Add user-specific master data for testing
-- =================================================================

-- Note:
-- nkraft_user_id = 1 is for 'test' user.
-- nkraft_user_id = 2 is for 'natsuki' user.

-- -----------------------------------------------------
-- Add sample accounts for user 'natsuki' (id=2)
-- -----------------------------------------------------
INSERT INTO `accounts` (`nkraft_user_id`, `account_name`, `balance`, `is_main`, `is_savings`) VALUES
(2, 'メイン', 0.00, 1, 0),
(2, '貯蓄', 0.00, 0, 1);

-- -----------------------------------------------------
-- Add sample categories for user 'natsuki' (id=2)
-- -----------------------------------------------------
INSERT INTO `categories` (`nkraft_user_id`, `category_name`) VALUES
(2, '給料'),
(2, '事業収入'),
(2, '家賃'),
(2, '食費'),
(2, '水道光熱費'),
(2, '通信費'),
(2, '交際費'),
(2, '雑費');