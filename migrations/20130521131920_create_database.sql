CREATE TABLE `channels` (
  `id` int(11)  NOT NULL AUTO_INCREMENT,
  `user_network_id` int(11)  NOT NULL,
  `name` varchar(128) COLLATE utf8_swedish_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_network_id` (`user_network_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_swedish_ci;

CREATE TABLE `log_lines` (
  `id` int(11)  NOT NULL AUTO_INCREMENT,
  `channel_id` int(11)  NOT NULL,
  `type` enum('msg','topic','part','join') COLLATE utf8_swedish_ci NOT NULL DEFAULT 'msg',
  `timestamp` datetime NOT NULL,
  `user` varchar(32) COLLATE utf8_swedish_ci DEFAULT NULL,
  `content` text COLLATE utf8_swedish_ci,
  PRIMARY KEY (`id`),
  KEY `channel_id` (`channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_swedish_ci;

CREATE TABLE `networks` (
  `id` int(11)  NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8_swedish_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_swedish_ci;

CREATE TABLE `servers` (
  `id` int(11)  NOT NULL AUTO_INCREMENT,
  `network_id` int(11)  NOT NULL,
  `address` varchar(256) COLLATE utf8_swedish_ci NOT NULL,
  `port` int(11)  NOT NULL,
  PRIMARY KEY (`id`),
  KEY `network_id` (`network_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_swedish_ci;

CREATE TABLE `users` (
  `id` int(11)  NOT NULL AUTO_INCREMENT,
  `nick` varchar(32) COLLATE utf8_swedish_ci NOT NULL,
  `fingerprint` varchar(64) COLLATE utf8_swedish_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nick` (`nick`,`fingerprint`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_swedish_ci;

CREATE TABLE `user_networks` (
  `id` int(11)  NOT NULL AUTO_INCREMENT,
  `user_id` int(11)  NOT NULL,
  `network_id` int(11)  NOT NULL,
  `nick` varchar(32) COLLATE utf8_swedish_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `network_id` (`network_id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_swedish_ci;


ALTER TABLE `channels`
  ADD CONSTRAINT `channels_ibfk_1` FOREIGN KEY (`user_network_id`) REFERENCES `user_networks` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `log_lines`
  ADD CONSTRAINT `log_lines_ibfk_1` FOREIGN KEY (`channel_id`) REFERENCES `channels` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `servers`
  ADD CONSTRAINT `servers_ibfk_1` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`);

ALTER TABLE `user_networks`
  ADD CONSTRAINT `user_networks_ibfk_2` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`),
  ADD CONSTRAINT `user_networks_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
