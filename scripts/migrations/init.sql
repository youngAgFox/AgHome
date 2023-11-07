CREATE SCHEMA `fridge` ;

CREATE TABLE migration (
id SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE PRIMARY KEY,
filename VARCHAR(255),
migration_date DATETIME,
revertable_ind CHAR(1) NOT NULL DEFAULT 'n' COMMENT 'Indicates if a migration is revertable.'
)
COMMENT 'Keeps records of migration scripts and relevent data.'