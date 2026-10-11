-- ==============================================================================
-- Peer-to-Peer Campus Marketplace Database Schema
-- Academic College Project
-- Compatible with MySQL 8.0+ and MariaDB 10.4+
-- Charset: utf8mb4, Collation: utf8mb4_unicode_ci
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS `campus_marketplace`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `campus_marketplace`;

-- Drop existing tables in reverse dependency order
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `admin_audit_logs`;
DROP TABLE IF EXISTS `reports`;
DROP TABLE IF EXISTS `wishlist`;
DROP TABLE IF EXISTS `listings`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------------------------
-- Table: users
-- Stores registered students and system administrators
-- ------------------------------------------------------------------------------
CREATE TABLE `users` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `student_id` VARCHAR(50) NOT NULL UNIQUE COMMENT 'College registration or roll number',
  `full_name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `department` VARCHAR(100) DEFAULT 'General Engineering',
  `year_of_study` INT DEFAULT 1,
  `role` ENUM('student', 'admin') NOT NULL DEFAULT 'student',
  `verification_status` ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending',
  `rejection_reason` VARCHAR(255) DEFAULT NULL,
  `profile_image` VARCHAR(255) DEFAULT NULL,
  `api_token` VARCHAR(128) DEFAULT NULL UNIQUE COMMENT 'Active session/API auth token',
  `token_expires_at` DATETIME DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_users_role_status` (`role`, `verification_status`),
  INDEX `idx_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- Table: categories
-- Categories for items traded on campus
-- ------------------------------------------------------------------------------
CREATE TABLE `categories` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  `slug` VARCHAR(100) NOT NULL UNIQUE,
  `description` VARCHAR(255) DEFAULT NULL,
  `icon_name` VARCHAR(50) DEFAULT 'box',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- Table: listings
-- Marketplace product listings created by verified students
-- ------------------------------------------------------------------------------
CREATE TABLE `listings` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `seller_id` INT UNSIGNED NOT NULL,
  `category_id` INT UNSIGNED NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT NOT NULL,
  `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  `condition_state` ENUM('New', 'Like New', 'Good', 'Acceptable') NOT NULL DEFAULT 'Good',
  `image_url` VARCHAR(500) DEFAULT NULL,
  `status` ENUM('available', 'sold', 'reserved', 'inactive') NOT NULL DEFAULT 'available',
  `views_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `contact_preference` VARCHAR(50) DEFAULT 'chat',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT,
  INDEX `idx_listings_status_created` (`status`, `created_at` DESC),
  INDEX `idx_listings_category` (`category_id`),
  INDEX `idx_listings_seller` (`seller_id`),
  FULLTEXT KEY `ft_listings_search` (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- Table: wishlist
-- Student saved items for quick access and tracking
-- ------------------------------------------------------------------------------
CREATE TABLE `wishlist` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT UNSIGNED NOT NULL,
  `listing_id` INT UNSIGNED NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_listing` (`user_id`, `listing_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`listing_id`) REFERENCES `listings` (`id`) ON DELETE CASCADE,
  INDEX `idx_wishlist_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- Table: reports
-- Listing moderation reports filed by students
-- ------------------------------------------------------------------------------
CREATE TABLE `reports` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `reporter_id` INT UNSIGNED NOT NULL,
  `listing_id` INT UNSIGNED NOT NULL,
  `reason` VARCHAR(150) NOT NULL,
  `details` TEXT DEFAULT NULL,
  `status` ENUM('pending', 'reviewed', 'dismissed') NOT NULL DEFAULT 'pending',
  `admin_notes` VARCHAR(255) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` DATETIME DEFAULT NULL,
  FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`listing_id`) REFERENCES `listings` (`id`) ON DELETE CASCADE,
  INDEX `idx_reports_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- Table: admin_audit_logs
-- Immutable security log of privileged administrative actions
-- ------------------------------------------------------------------------------
CREATE TABLE `admin_audit_logs` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `admin_id` INT UNSIGNED NOT NULL,
  `action` VARCHAR(100) NOT NULL COMMENT 'e.g. APPROVE_STUDENT, REJECT_STUDENT, REMOVE_LISTING',
  `target_type` VARCHAR(50) NOT NULL COMMENT 'e.g. user, listing, report',
  `target_id` INT UNSIGNED NOT NULL,
  `details` TEXT DEFAULT NULL,
  `ip_address` VARCHAR(45) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  INDEX `idx_audit_admin` (`admin_id`),
  INDEX `idx_audit_created` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
