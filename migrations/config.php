<?php
/* Mandatory: config.php MUST implement Config::fix_database($username=null)
 * where $username is set from command line (but it is optional)
 *
 * You can make username on command line mandatory, just throw an
 * exception in fix_database with a relevant message.
 *
 * Config::fix_database() MUST return an object compatible with MySQLi
 * (you may want to subclass MySQLi if you need something special).
 *
 * If you want PHP migrations to work, you need to include all relevant
 * environment here.
 *
 */

// require "/path/to/relevant/includes.php";

class Config {
	public static function fix_database($username=null) {
		$file_dir = realpath(dirname(__FILE__));

		$f = fopen("$file_dir/../server/server.cfg", "r");
		if(!$f) {
			throw new Exception("Failed to open server.cfg");
		}

		while(($line = fgets($f)) != null) {
			$matches = array();
			if(!preg_match("/([^= ]+) ?= ?(.+)/i", $line, $matches)) {
				throw new Exception("Malformed config line: $line");
			}
			$key = $matches[1]; $val = $matches[2];

			if($key == "database.url") {
				if(preg_match("#jdbc:mysql://([^:]+):[0-9]+/(.+)#i", $val, $matches)) {
					$db_host = $matches[1];
					$db_name = $matches[2];
				} else {
					throw new Exception("Couldn't parse database.url");
				}
			} else if($key == "database.username") {
				$cfg_username = $val;
			} else if($key == "database.password") {
				$cfg_password = $val;
			}
		}
		fclose($f);

		if(is_null($username)) {
			$username = $cfg_username;
			$password = $cfg_password;
		} else {
			$password = ask_for_password();
		}
		$db = new MySQLi($db_host, $username, $password, $db_name);
		return $db;
	}

	/**
	 * Return an array of RE patterns of files to ignore.
	 */
	public static function ignored(){
		return array();
	}

	/*
	 * These hooks are called in different stages of the update_migration execution:
	 */

	/**
	 * Called before any migrations are run, but after database initialization
	 */
	public static function begin_hook() {

	}

	/**
	 * Called after all migrations are completed
	 */
	public static function end_hook() {

	}

	/**
	 * Called before each migration are run
	 * @param $migration_name The name of the migration to be run
	 */
	public static function pre_migration_hook($migration_name) {

	}

	/**
	 * Called after each migration have succeded
	 * @param $migration_name The name of the migration that succeded
	 */
	public static function post_migration_hook($migration_name) {

	}
	/*
	 * Called after a migration rollback has occurred, just before exit()
	 * @param $migration_name The name of the migration that caused the rollback
	 */

	public static function post_rollback_hook($migration_name) {

	}


}
