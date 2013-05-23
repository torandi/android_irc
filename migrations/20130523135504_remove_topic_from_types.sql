ALTER TABLE `log_lines` CHANGE COLUMN `type` `type` enum('MSG','PART','JOIN') NOT NULL DEFAULT 'MSG';
