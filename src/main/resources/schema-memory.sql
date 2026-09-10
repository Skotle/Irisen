-- Schema-only snapshot of the 39 local SQLite tables; no user data.
CREATE TABLE `account_verification` (
  `request_id` INTEGER PRIMARY KEY,
  `uid` varchar(50) NOT NULL,
  `email` varchar(191) NOT NULL,
  `action_type` varchar(30) NOT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `verification_code` varchar(20) NOT NULL,
  `expires_at` datetime NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `alarm` (
  `alarm_id` INTEGER PRIMARY KEY,
  `uid` varchar(50) NOT NULL,
  `alarm_type` varchar(50) NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` text,
  `ref_type` varchar(50) DEFAULT NULL,
  `ref_id` varchar(100) DEFAULT NULL,
  `ref_gall_id` varchar(50) DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `read_at` datetime DEFAULT NULL,
  CONSTRAINT `alarm_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `user` (`uid`),
  CONSTRAINT `alarm_ibfk_2` FOREIGN KEY (`ref_gall_id`) REFERENCES `board` (`gall_id`)
);

CREATE TABLE `app_seed_history` (
  `seed_key` varchar(120) NOT NULL,
  `post_count` int NOT NULL DEFAULT '0',
  `comment_count` int NOT NULL DEFAULT '0',
  `note` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`seed_key`)
);

CREATE TABLE `board` (
  `gall_id` varchar(50) NOT NULL,
  `gall_name` varchar(100) NOT NULL,
  `gall_type` varchar(10) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `manager_uid` varchar(50) DEFAULT NULL,
  `post_count` int DEFAULT '0',
  `status` varchar(20) NOT NULL DEFAULT 'active',
  `last_activity_at` datetime DEFAULT NULL,
  `dormant_notified_at` datetime DEFAULT NULL,
  `dormant_at` datetime DEFAULT NULL,
  `topic_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`gall_id`),
  CONSTRAINT `board_ibfk_1` FOREIGN KEY (`manager_uid`) REFERENCES `user` (`uid`)
);

CREATE TABLE `board_ban` (
  `ban_id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) NOT NULL,
  `target_uid` varchar(50) DEFAULT NULL,
  `target_ip` varchar(45) DEFAULT NULL,
  `banned_by` varchar(50) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `banned_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` datetime DEFAULT NULL,
  CONSTRAINT `board_ban_ibfk_1` FOREIGN KEY (`gall_id`) REFERENCES `board` (`gall_id`),
  CONSTRAINT `board_ban_ibfk_2` FOREIGN KEY (`target_uid`) REFERENCES `user` (`uid`),
  CONSTRAINT `board_ban_ibfk_3` FOREIGN KEY (`banned_by`) REFERENCES `user` (`uid`),
  CONSTRAINT `chk_ban_target` CHECK (((`target_uid` is not null) or (`target_ip` is not null)))
);

CREATE TABLE `board_counter` (
  `gall_id` varchar(50) NOT NULL,
  `last_post_no` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`gall_id`)
);

CREATE TABLE `board_join_request` (
  `request_id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) NOT NULL,
  `uid` varchar(50) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'pending',
  `reason` text,
  `reviewed_by` varchar(50) DEFAULT NULL,
  `reviewed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `board_member` (
  `gall_id` varchar(50) NOT NULL,
  `uid` varchar(50) NOT NULL,
  `member_role` varchar(30) NOT NULL DEFAULT 'member',
  `status` varchar(20) NOT NULL DEFAULT 'active',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `approved_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`gall_id`,`uid`)
);

CREATE TABLE `board_ranking_refresh_state` (
  `ranking_date` date NOT NULL,
  `refresh_count` int NOT NULL DEFAULT '0',
  `last_refreshed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`ranking_date`)
);

CREATE TABLE `board_ranking_snapshot` (
  `ranking_date` date NOT NULL,
  `rank_no` int NOT NULL,
  `gall_id` varchar(50) NOT NULL,
  `gall_name` varchar(100) NOT NULL,
  `gall_type` varchar(10) DEFAULT NULL,
  `score` bigint NOT NULL DEFAULT '0',
  `post_count` int NOT NULL DEFAULT '0',
  `recent_post_count` int NOT NULL DEFAULT '0',
  `recent_recommend_sum` int NOT NULL DEFAULT '0',
  `refreshed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ranking_date`,`rank_no`)
);

CREATE TABLE `board_request` (
  `request_id` INTEGER PRIMARY KEY,
  `requester_uid` varchar(50) NOT NULL,
  `gall_id` varchar(50) DEFAULT NULL,
  `gall_name` varchar(100) NOT NULL,
  `gall_type` varchar(10) NOT NULL DEFAULT 'm',
  `reason` text,
  `status` varchar(20) NOT NULL DEFAULT 'pending',
  `reviewed_by` varchar(50) DEFAULT NULL,
  `reviewed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `topic_id` varchar(50) DEFAULT NULL,
  CONSTRAINT `board_request_ibfk_1` FOREIGN KEY (`requester_uid`) REFERENCES `user` (`uid`),
  CONSTRAINT `board_request_ibfk_2` FOREIGN KEY (`reviewed_by`) REFERENCES `user` (`uid`)
);

CREATE TABLE `board_submanager` (
  `gall_id` varchar(50) NOT NULL,
  `uid` varchar(50) NOT NULL,
  `appointed_by` varchar(50) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'pending',
  `responded_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `can_delete_post` tinyint(1) NOT NULL DEFAULT '1',
  `can_delete_comment` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_write` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_guest_penalty` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_tags` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_images` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_notice` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_categories` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_cover` tinyint(1) NOT NULL DEFAULT '1',
  `can_ban_user` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_forbidden_word` tinyint(1) NOT NULL DEFAULT '1',
  `can_bump_post` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_concept` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_concept_cut` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_submanager` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`gall_id`,`uid`)
);

CREATE TABLE `board_tag` (
  `tag_id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) NOT NULL,
  `tag_name` varchar(50) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `board_tag_ibfk_1` FOREIGN KEY (`gall_id`) REFERENCES `board` (`gall_id`)
);

CREATE TABLE `board_topic` (
  `topic_id` varchar(50) NOT NULL,
  `topic_name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`topic_id`)
);

CREATE TABLE `board_transfer` (
  `transfer_id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) NOT NULL,
  `from_uid` varchar(50) NOT NULL,
  `to_uid` varchar(50) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'pending',
  `responded_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `board_transfer_ibfk_1` FOREIGN KEY (`gall_id`) REFERENCES `board` (`gall_id`),
  CONSTRAINT `board_transfer_ibfk_2` FOREIGN KEY (`from_uid`) REFERENCES `user` (`uid`),
  CONSTRAINT `board_transfer_ibfk_3` FOREIGN KEY (`to_uid`) REFERENCES `user` (`uid`)
);

CREATE TABLE `comment` (
  `id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) NOT NULL,
  `post_no` int NOT NULL,
  `parent_id` int DEFAULT NULL,
  `writer_uid` varchar(50) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reply_depth` int NOT NULL DEFAULT '0',
  `sort_key` varchar(255) DEFAULT NULL,
  `like_count` int NOT NULL DEFAULT '0',
  `report_count` int NOT NULL DEFAULT '0',
  `review_status` varchar(20) NOT NULL DEFAULT 'normal',
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`gall_id`) REFERENCES `board` (`gall_id`),
  CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`),
  CONSTRAINT `comment_ibfk_3` FOREIGN KEY (`writer_uid`) REFERENCES `user` (`uid`)
);

CREATE TABLE `comment_reaction` (
  `comment_id` bigint NOT NULL,
  `actor_key` varchar(150) NOT NULL,
  `reaction_type` varchar(20) NOT NULL DEFAULT 'like',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`,`actor_key`)
);

CREATE TABLE `comment_report` (
  `report_id` INTEGER PRIMARY KEY,
  `comment_id` bigint NOT NULL,
  `reporter_key` varchar(150) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `forbidden_word` (
  `word_id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) DEFAULT NULL,
  `word` varchar(100) NOT NULL,
  `action` varchar(20) NOT NULL DEFAULT 'block',
  `created_by` varchar(50) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `gallery_counter` (
  `gall_id` varchar(50) NOT NULL,
  `last_post_no` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`gall_id`)
);

CREATE TABLE `gallery_setting` (
  `gall_id` varchar(50) NOT NULL,
  `board_notice` text,
  `welcome_message` text,
  `welcome_image_url` varchar(500) DEFAULT NULL,
  `theme_color` varchar(20) NOT NULL DEFAULT '#ff8fab',
  `allow_guest_post` tinyint(1) NOT NULL DEFAULT '1',
  `allow_guest_comment` tinyint(1) NOT NULL DEFAULT '1',
  `concept_recommend_threshold` int NOT NULL DEFAULT '10',
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `category_options` text,
  `join_policy` varchar(20) NOT NULL DEFAULT 'free',
  `visibility` varchar(20) NOT NULL DEFAULT 'public',
  `pinned_notice_count` int NOT NULL DEFAULT '3',
  `allowed_attachment_types` varchar(255) DEFAULT NULL,
  `attachment_max_bytes` bigint NOT NULL DEFAULT '10485760',
  `side_board_approval_policy` varchar(20) NOT NULL DEFAULT 'operator',
  `dormant_after_days` int NOT NULL DEFAULT '180',
  `cover_image_url` varchar(500) DEFAULT NULL,
  `allow_member_image` tinyint(1) NOT NULL DEFAULT '1',
  `allow_guest_image` tinyint(1) NOT NULL DEFAULT '0',
  `board_tags` text,
  `read_visibility` varchar(20) NOT NULL DEFAULT 'inherit',
  PRIMARY KEY (`gall_id`),
  CONSTRAINT `gallery_setting_ibfk_1` FOREIGN KEY (`gall_id`) REFERENCES `board` (`gall_id`),
  CONSTRAINT `gallery_setting_ibfk_2` FOREIGN KEY (`updated_by`) REFERENCES `user` (`uid`)
);

CREATE TABLE `moderation_log` (
  `log_id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) DEFAULT NULL,
  `actor_uid` varchar(50) DEFAULT NULL,
  `target_uid` varchar(50) DEFAULT NULL,
  `target_ip` varchar(45) DEFAULT NULL,
  `action_type` varchar(50) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `permissions` (
  `perm_id` INTEGER PRIMARY KEY,
  `perm_name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL
);

CREATE TABLE `post` (
  `id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) NOT NULL,
  `post_no` int NOT NULL,
  `tag_id` int DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `content` text,
  `writer_uid` varchar(50) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `view_count` int DEFAULT '0',
  `comment_count` int DEFAULT '0',
  `recommend_count` int DEFAULT '0',
  `unrecommend_count` int DEFAULT '0',
  `is_concept` tinyint(1) NOT NULL DEFAULT '0',
  `concept_at` datetime DEFAULT NULL,
  `concept_cancelled_by` varchar(50) DEFAULT NULL,
  `concept_cancelled_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `writed_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `category` varchar(50) DEFAULT NULL,
  `is_draft` tinyint(1) NOT NULL DEFAULT '0',
  `is_secret` tinyint(1) NOT NULL DEFAULT '0',
  `review_status` varchar(20) NOT NULL DEFAULT 'normal',
  `report_count` int NOT NULL DEFAULT '0',
  `pinned_at` datetime DEFAULT NULL,
  `pin_order` int DEFAULT NULL,
  `attachment_urls` text,
  `is_notice` tinyint(1) NOT NULL DEFAULT '0',
  `concept_target_count` int DEFAULT NULL,
  `concept_manual_state` varchar(20) NOT NULL DEFAULT 'auto',
  `bumped_at` datetime DEFAULT NULL,
  CONSTRAINT `post_ibfk_1` FOREIGN KEY (`gall_id`) REFERENCES `board` (`gall_id`),
  CONSTRAINT `post_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `board_tag` (`tag_id`),
  CONSTRAINT `post_ibfk_3` FOREIGN KEY (`writer_uid`) REFERENCES `user` (`uid`),
  CONSTRAINT `post_ibfk_4` FOREIGN KEY (`concept_cancelled_by`) REFERENCES `user` (`uid`)
);

CREATE TABLE `post_attachment` (
  `attachment_id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) NOT NULL,
  `post_no` bigint NOT NULL,
  `url` varchar(1000) NOT NULL,
  `file_type` varchar(100) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `post_content` (
  `id` INTEGER PRIMARY KEY,
  `post_id` bigint NOT NULL,
  `gall_id` varchar(50) NOT NULL,
  `post_no` bigint NOT NULL,
  `content` mediumtext,
  `content_format` varchar(30) NOT NULL DEFAULT 'html',
  `render_policy` varchar(30) NOT NULL DEFAULT 'trusted_html',
  `allow_images` tinyint(1) NOT NULL DEFAULT '1',
  `image_count` int NOT NULL DEFAULT '0',
  `word_count` int NOT NULL DEFAULT '0',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `post_report` (
  `report_id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) NOT NULL,
  `post_no` bigint NOT NULL,
  `reporter_key` varchar(150) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `post_scrap` (
  `uid` varchar(50) NOT NULL,
  `gall_id` varchar(50) NOT NULL,
  `post_no` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`uid`,`gall_id`,`post_no`)
);

CREATE TABLE `post_vote` (
  `id` INTEGER PRIMARY KEY,
  `gall_id` varchar(50) NOT NULL,
  `post_no` bigint NOT NULL,
  `actor_key` varchar(150) NOT NULL,
  `vote_type` varchar(20) NOT NULL,
  `vote_date` date NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `role_permissions` (
  `role_id` int NOT NULL,
  `perm_id` int NOT NULL,
  PRIMARY KEY (`role_id`,`perm_id`),
  CONSTRAINT `role_permissions_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`),
  CONSTRAINT `role_permissions_ibfk_2` FOREIGN KEY (`perm_id`) REFERENCES `permissions` (`perm_id`)
);

CREATE TABLE `roles` (
  `role_id` INTEGER PRIMARY KEY,
  `role_name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL
);

CREATE TABLE `signup_verification` (
  `request_id` INTEGER PRIMARY KEY,
  `uid` varchar(50) NOT NULL,
  `nick` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `verification_code` varchar(20) NOT NULL,
  `expires_at` datetime NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `nick_type` varchar(20) NOT NULL DEFAULT 'variable'
);

CREATE TABLE `user` (
  `uid` varchar(50) NOT NULL,
  `nick` varchar(50) NOT NULL,
  `nick_icon_type` varchar(20) DEFAULT 'default',
  `password_hash` varchar(255) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `member_division` varchar(20) DEFAULT 'user',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `nick_type` varchar(20) NOT NULL DEFAULT 'variable',
  PRIMARY KEY (`uid`)
);

CREATE TABLE `user_block` (
  `blocker_uid` varchar(50) NOT NULL,
  `blocked_uid` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`blocker_uid`,`blocked_uid`)
);

CREATE TABLE `user_follow` (
  `follower_uid` varchar(50) NOT NULL,
  `following_uid` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`follower_uid`,`following_uid`),
  CONSTRAINT `user_follow_ibfk_1` FOREIGN KEY (`follower_uid`) REFERENCES `user` (`uid`),
  CONSTRAINT `user_follow_ibfk_2` FOREIGN KEY (`following_uid`) REFERENCES `user` (`uid`)
);

CREATE TABLE `user_notification_setting` (
  `uid` varchar(50) NOT NULL,
  `in_app_enabled` tinyint(1) NOT NULL DEFAULT '1',
  `email_enabled` tinyint(1) NOT NULL DEFAULT '0',
  `follow_post_enabled` tinyint(1) NOT NULL DEFAULT '1',
  `comment_enabled` tinyint(1) NOT NULL DEFAULT '1',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`uid`)
);

CREATE TABLE `user_profile` (
  `profile_id` INTEGER PRIMARY KEY,
  `uid` varchar(50) NOT NULL,
  `profile_image` varchar(500) DEFAULT NULL,
  `bio` text,
  `birthdate` date DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `website_url` varchar(300) DEFAULT NULL,
  `sns_instagram` varchar(100) DEFAULT NULL,
  `sns_x` varchar(100) DEFAULT NULL,
  `sns_youtube` varchar(200) DEFAULT NULL,
  `sns_github` varchar(100) DEFAULT NULL,
  `is_public` tinyint(1) NOT NULL DEFAULT '1',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `user_profile_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `user` (`uid`)
);

CREATE TABLE `user_profile_setting` (
  `uid` varchar(50) NOT NULL,
  `status_message` varchar(160) DEFAULT NULL,
  `bio` text,
  `accent_color` varchar(20) NOT NULL DEFAULT '#ff8fab',
  `show_posts` tinyint(1) NOT NULL DEFAULT '1',
  `show_comments` tinyint(1) NOT NULL DEFAULT '1',
  `show_birthdate` tinyint(1) NOT NULL DEFAULT '1',
  `show_gender` tinyint(1) NOT NULL DEFAULT '1',
  `show_sns` tinyint(1) NOT NULL DEFAULT '1',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `avatar_url` varchar(500) DEFAULT NULL,
  `banner_url` varchar(500) DEFAULT NULL,
  `show_followers` tinyint(1) NOT NULL DEFAULT '1',
  `show_following` tinyint(1) NOT NULL DEFAULT '1', avatar_position VARCHAR(10) NOT NULL DEFAULT 'center', banner_position VARCHAR(10) NOT NULL DEFAULT 'center',
  PRIMARY KEY (`uid`),
  CONSTRAINT `user_profile_setting_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `user` (`uid`)
);

CREATE TABLE `user_suspension` (
  `uid` varchar(50) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `suspended_by` varchar(50) DEFAULT NULL,
  `suspended_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` datetime DEFAULT NULL,
  PRIMARY KEY (`uid`)
);
