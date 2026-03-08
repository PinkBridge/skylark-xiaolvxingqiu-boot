-- Garden-centric schema (without local user table).
-- user_id references external identity service (permission).

CREATE TABLE IF NOT EXISTS `garden` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT 'external user id from permission service',
  `name` VARCHAR(64) NOT NULL,
  `established_date` DATE NOT NULL,
  `thumb_url` VARCHAR(512) DEFAULT NULL,
  `cover_url` VARCHAR(512) DEFAULT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `is_default` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_garden_user_id` (`user_id`),
  KEY `idx_garden_user_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `plant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT 'external user id from permission service',
  `garden_id` BIGINT NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `species` VARCHAR(64) DEFAULT NULL,
  `image_url` VARCHAR(512) DEFAULT NULL,
  `cultivation_type` VARCHAR(16) NOT NULL,
  `planting_date` DATE DEFAULT NULL,
  `note` VARCHAR(500) DEFAULT NULL,
  `health_status` VARCHAR(16) NOT NULL DEFAULT 'healthy',
  `is_favorite` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_plant_user_garden` (`user_id`, `garden_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `plant_focus` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `plant_id` BIGINT NOT NULL,
  `reason` VARCHAR(300) NOT NULL,
  `photo_url` VARCHAR(512) NOT NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_focus_user_plant` (`user_id`, `plant_id`),
  KEY `idx_focus_user_id` (`user_id`),
  KEY `idx_focus_plant_id` (`plant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `plant_status_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `plant_id` BIGINT NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `changed_at` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_status_log_user_plant_time` (`user_id`, `plant_id`, `changed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `care_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `plant_id` BIGINT NOT NULL,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `seasonal_mode` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_care_plan_user_plant` (`user_id`, `plant_id`),
  KEY `idx_care_plan_user_enabled` (`user_id`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `care_plan_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `plan_id` BIGINT NOT NULL,
  `activity_type` VARCHAR(16) NOT NULL,
  `season` VARCHAR(8) NOT NULL DEFAULT 'ALL',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `interval_days` INT NOT NULL,
  `next_due_date` DATE DEFAULT NULL,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rule_user_plan` (`user_id`, `plan_id`),
  KEY `idx_rule_next_due` (`next_due_date`, `enabled`),
  KEY `idx_rule_type_season` (`activity_type`, `season`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `care_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `plant_id` BIGINT NOT NULL,
  `plan_id` BIGINT DEFAULT NULL,
  `rule_id` BIGINT DEFAULT NULL,
  `activity_type` VARCHAR(16) NOT NULL,
  `scheduled_date` DATE NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  `completed_at` DATETIME DEFAULT NULL,
  `record_json` JSON DEFAULT NULL,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_activity_user_date_status` (`user_id`, `scheduled_date`, `status`),
  KEY `idx_activity_user_plant_date` (`user_id`, `plant_id`, `scheduled_date`),
  KEY `idx_activity_rule_date` (`rule_id`, `scheduled_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `contact` VARCHAR(100) DEFAULT NULL,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_feedback_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_profile` (
  `user_id` BIGINT NOT NULL,
  `avatar` VARCHAR(512) DEFAULT NULL,
  `name` VARCHAR(64) NOT NULL,
  `gender` VARCHAR(16) DEFAULT NULL,
  `birthday` VARCHAR(20) DEFAULT NULL,
  `motto` VARCHAR(200) DEFAULT NULL,
  `phone` VARCHAR(32) DEFAULT NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_coin_account` (
  `user_id` BIGINT NOT NULL,
  `coin_balance` BIGINT NOT NULL DEFAULT 0,
  `completed_activity_total` BIGINT NOT NULL DEFAULT 0,
  `progress_activity_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_coin_txn` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `change_amount` BIGINT NOT NULL DEFAULT 0,
  `reason` VARCHAR(64) NOT NULL,
  `related_date` DATE NOT NULL,
  `meta_json` JSON DEFAULT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coin_txn_user_reason_date` (`user_id`, `reason`, `related_date`),
  KEY `idx_coin_txn_user_id` (`user_id`),
  KEY `idx_coin_txn_date` (`related_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
