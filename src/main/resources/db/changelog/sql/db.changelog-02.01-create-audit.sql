CREATE TABLE `audit_entity_logs` (
  `id` int NOT NULL,
  `entity_id` int DEFAULT NULL,
  `entity_name` varchar(255) DEFAULT NULL,
  `operated_at` datetime DEFAULT NULL,
  `operation` varchar(255) DEFAULT NULL,
  `operated_by_user_id` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `audit_update_logs` (
  `id` int NOT NULL,
  `op` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `previous_value` varchar(255) DEFAULT NULL,
  `log_id` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


ALTER TABLE `audit_entity_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKlp309s0ux2cjtmghhjhfm3kgg` (`operated_by_user_id`);

ALTER TABLE `audit_update_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKkl4s7bdqoycbmton0c1h1i13e` (`log_id`);


ALTER TABLE `audit_entity_logs`
  ADD CONSTRAINT `FKlp309s0ux2cjtmghhjhfm3kgg` FOREIGN KEY (`operated_by_user_id`) REFERENCES `users` (`id`);

ALTER TABLE `audit_update_logs`
  ADD CONSTRAINT `FKkl4s7bdqoycbmton0c1h1i13e` FOREIGN KEY (`log_id`) REFERENCES `audit_entity_logs` (`id`);
