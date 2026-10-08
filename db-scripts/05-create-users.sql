-- -----------------------------------------------------
-- Users for authentication (added on top of the course schema)
-- Run after 04-create-order-tables.sql.
-- The first admin account is created by the backend at startup
-- (app.admin.email / app.admin.password in application.properties).
-- -----------------------------------------------------

USE `full-stack-ecommerce`;

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'USER',
  `date_created` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
