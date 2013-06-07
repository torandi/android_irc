ALTER TABLE `user_networks` DROP FOREIGN KEY `user_networks_ibfk_2`;

ALTER TABLE `user_networks` DROP COLUMN `network_id`;

ALTER TABLE `user_networks` ADD `address` VARCHAR( 128 ) NOT NULL ,
ADD `port` INT NOT NULL DEFAULT 6667;

DROP TABLE servers;
DROP TABLE networks;


