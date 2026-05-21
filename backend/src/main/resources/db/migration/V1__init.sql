-- 1. 회원 (Member)
CREATE TABLE `member` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `name` VARCHAR(255) NOT NULL,
    `profile_image_url` VARCHAR(255),
    `role` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `password_changed_at` DATETIME,
    `policy_agreed` TINYINT(1) NOT NULL DEFAULT 0,
    `subscribed` TINYINT(1) NOT NULL DEFAULT 0,
    `oauth_provider` VARCHAR(50),
    `created_at` DATETIME,
    `updated_at` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 지역 정보 (TourRegion)
CREATE TABLE `tour_region` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `areaCode` VARCHAR(50),
    `areaName` VARCHAR(100),
    `siGunGuCode` VARCHAR(50),
    `siGunGuName` VARCHAR(100),
    `region` VARCHAR(100),
    CONSTRAINT `uq_area_sigungu` UNIQUE (`areaCode`, `siGunGuCode`),
    INDEX `idx_tourregion_region` (`region`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 장소 카테고리 (PlaceCategory)
CREATE TABLE `place_category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `cat3` VARCHAR(50) NOT NULL UNIQUE,
    `contentTypeId` VARCHAR(50),
    `cat1` VARCHAR(50),
    `cat2` VARCHAR(50),
    `large_classification` VARCHAR(100),
    `mid_classification` VARCHAR(100),
    `small_classification` VARCHAR(100),
    `theme` VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 게시판 (Board)
CREATE TABLE `board` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(50) NOT NULL,
    `content` MEDIUMTEXT NOT NULL,
    `member_id` BIGINT NOT NULL,
    `board_hits` INT NOT NULL DEFAULT 0,
    `theme` VARCHAR(255) NOT NULL,
    `region` VARCHAR(255) NOT NULL,
    `thumbnail_public_url` VARCHAR(255),
    `created_at` DATETIME,
    `updated_at` DATETIME,
    CONSTRAINT `fk_board_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 댓글 (Comment)
CREATE TABLE `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `member_id` BIGINT NOT NULL,
    `comment_content` VARCHAR(255) NOT NULL,
    `parentcomment_id` BIGINT,
    `board_id` BIGINT,
    `created_at` DATETIME,
    `updated_at` DATETIME,
    CONSTRAINT `fk_comment_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parentcomment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_board` FOREIGN KEY (`board_id`) REFERENCES `board` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 그룹 (Group)
CREATE TABLE `user_groups` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `group_name` VARCHAR(255) NOT NULL UNIQUE,
    `created_by` BIGINT NOT NULL,
    `description` VARCHAR(255),
    CONSTRAINT `fk_groups_creator` FOREIGN KEY (`created_by`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 그룹 멤버 (ManyToMany 조인 테이블)
CREATE TABLE `user_groups_members` (
    `user_groups_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_groups_id`, `user_id`),
    CONSTRAINT `fk_ugm_group` FOREIGN KEY (`user_groups_id`) REFERENCES `user_groups` (`id`),
    CONSTRAINT `fk_ugm_member` FOREIGN KEY (`user_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 그룹 공지사항 (GroupAnnouncement)
CREATE TABLE `group_announcement` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_groups_id` BIGINT NOT NULL,
    `writer_name` VARCHAR(255),
    `title` TEXT NOT NULL,
    `content` TEXT,
    `time_stamp` DATETIME NOT NULL,
    CONSTRAINT `fk_ga_group` FOREIGN KEY (`user_groups_id`) REFERENCES `user_groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 그룹 초대 (GroupInvitation)
CREATE TABLE `group_invitation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `invitation_code` VARCHAR(255) NOT NULL UNIQUE,
    `user_groups_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `expires_at` DATETIME NOT NULL,
    `active` TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT `fk_gi_group` FOREIGN KEY (`user_groups_id`) REFERENCES `user_groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 채팅 메시지 (ChatMessage)
CREATE TABLE `chat_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `group_id` BIGINT NOT NULL,
    `sender_id` BIGINT NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    `content` TEXT,
    `created_at` DATETIME,
    `updated_at` DATETIME,
    CONSTRAINT `fk_chat_group` FOREIGN KEY (`group_id`) REFERENCES `user_groups` (`id`),
    CONSTRAINT `fk_chat_sender` FOREIGN KEY (`sender_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 일정 (Schedule)
CREATE TABLE `schedule` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `start_schedule` DATETIME NOT NULL,
    `end_schedule` DATETIME NOT NULL,
    `group_id` BIGINT NOT NULL UNIQUE,
    `created_at` DATETIME,
    `updated_at` DATETIME,
    CONSTRAINT `fk_schedule_group` FOREIGN KEY (`group_id`) REFERENCES `user_groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. 세부 일정 장소 (SchedulePlace)
CREATE TABLE `schedule_place` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `schedule_id` BIGINT NOT NULL,
    `content_id` VARCHAR(255) NOT NULL,
    `place_type` VARCHAR(50) NOT NULL,
    `visit_start` DATETIME NOT NULL,
    `visited_end` DATETIME NOT NULL,
    `day_order` INT NOT NULL,
    `order_in_day` INT NOT NULL,
    `created_at` DATETIME,
    `updated_at` DATETIME,
    CONSTRAINT `fk_sp_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. 숙소 (Accommodation)
CREATE TABLE `accommodation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `content_id` VARCHAR(255) NOT NULL UNIQUE,
    `title` VARCHAR(255),
    `addr1` VARCHAR(255),
    `addr2` VARCHAR(255),
    `areaCode` VARCHAR(50),
    `siGunGuCode` VARCHAR(50),
    `cat1` VARCHAR(50),
    `cat2` VARCHAR(50),
    `cat3` VARCHAR(50),
    `image_url` VARCHAR(255),
    `thumbnail_image_url` VARCHAR(255),
    `map_x` DOUBLE,
    `map_y` DOUBLE,
    `m_level` INT,
    `tel` VARCHAR(50),
    `created_time` VARCHAR(255),
    `modified_time` VARCHAR(255),
    `tour_region_id` BIGINT,
    `place_category_id` BIGINT,
    CONSTRAINT `fk_acc_region` FOREIGN KEY (`tour_region_id`) REFERENCES `tour_region` (`id`),
    CONSTRAINT `fk_acc_category` FOREIGN KEY (`place_category_id`) REFERENCES `place_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. 식당 (Restaurant)
CREATE TABLE `restaurant` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `content_id` VARCHAR(255) NOT NULL UNIQUE,
    `title` VARCHAR(255),
    `addr1` VARCHAR(255),
    `addr2` VARCHAR(255),
    `areaCode` VARCHAR(50),
    `siGunGuCode` VARCHAR(50),
    `cat1` VARCHAR(50),
    `cat2` VARCHAR(50),
    `cat3` VARCHAR(50),
    `image_url` VARCHAR(255),
    `thumbnail_image_url` VARCHAR(255),
    `map_x` DOUBLE,
    `map_y` DOUBLE,
    `m_level` INT,
    `tel` VARCHAR(50),
    `created_time` VARCHAR(255),
    `modified_time` VARCHAR(255),
    `tour_region_id` BIGINT,
    `place_category_id` BIGINT,
    CONSTRAINT `fk_res_region` FOREIGN KEY (`tour_region_id`) REFERENCES `tour_region` (`id`),
    CONSTRAINT `fk_res_category` FOREIGN KEY (`place_category_id`) REFERENCES `place_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. 관광지 (TouristSpot)
CREATE TABLE `tourist_spot` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `content_id` VARCHAR(255) NOT NULL UNIQUE,
    `title` VARCHAR(255),
    `addr1` VARCHAR(255),
    `addr2` VARCHAR(255),
    `areaCode` VARCHAR(50),
    `siGunGuCode` VARCHAR(50),
    `cat1` VARCHAR(50),
    `cat2` VARCHAR(50),
    `cat3` VARCHAR(50),
    `image_url` VARCHAR(255),
    `thumbnail_image_url` VARCHAR(255),
    `map_x` DOUBLE,
    `map_y` DOUBLE,
    `m_level` INT,
    `tel` VARCHAR(50),
    `created_time` VARCHAR(255),
    `modified_time` VARCHAR(255),
    `tour_region_id` BIGINT,
    `place_category_id` BIGINT,
    CONSTRAINT `fk_ts_region` FOREIGN KEY (`tour_region_id`) REFERENCES `tour_region` (`id`),
    CONSTRAINT `fk_ts_category` FOREIGN KEY (`place_category_id`) REFERENCES `place_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
