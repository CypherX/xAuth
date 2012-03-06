CREATE TABLE IF NOT EXISTS `{TABLE}` (
	`playername` VARCHAR(255) NOT NULL,
	`items` TEXT NOT NULL,
    `armor` TEXT NOT NULL,
    `location` TEXT NOT NULL,
	PRIMARY KEY (`playername`)
);