CREATE TABLE `notification`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `receiver_id` BIGINT       NOT NULL,
    `actor_id`    BIGINT NULL,
    `type`        VARCHAR(50)  NOT NULL,
    `message`     VARCHAR(500) NOT NULL,
    `target_id`   BIGINT NULL,
    `group_name`  VARCHAR(255) NULL,
    `is_read`     TINYINT(1)   NOT NULL DEFAULT 0,
    `read_at`     DATETIME NULL,
    `created_at`  DATETIME,
    `updated_at`  DATETIME,
    CONSTRAINT `fk_notification_receiver` FOREIGN KEY (`receiver_id`)
        REFERENCES `member` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_notification_actor` FOREIGN KEY (`actor_id`)
        REFERENCES `member` (`id`) ON DELETE SET NULL,
    INDEX         `idx_notification_receiver_id` (`receiver_id`, `id`),
    INDEX         `idx_notification_receiver_unread` (`receiver_id`, `is_read`),
    INDEX         `idx_notification_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `schedule_reminder_log`
(
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `schedule_id`   BIGINT      NOT NULL,
    `reminder_type` VARCHAR(20) NOT NULL,
    `reminder_date` DATE        NOT NULL,
    `created_at`    DATETIME,
    `updated_at`    DATETIME,
    CONSTRAINT `uk_schedule_reminder` UNIQUE (`schedule_id`, `reminder_type`, `reminder_date`),
    CONSTRAINT `fk_srl_schedule` FOREIGN KEY (`schedule_id`)
        REFERENCES `schedule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;