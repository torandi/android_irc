ALTER TABLE `users`
	DROP COLUMN fingerprint,
	ADD COLUMN user varchar(128) NOT NULL;
