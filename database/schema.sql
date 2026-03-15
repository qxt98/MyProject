-- 药品信息智能化管理系统 - 数据库脚本
-- MySQL 8.0+，字符集 utf8mb4
-- Docker 初始化时依赖 MYSQL_DATABASE=smart_pharma，此处显式指定库
USE smart_pharma;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 药品信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `drug` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(200) NOT NULL COMMENT '药品名称',
    `category` varchar(50) DEFAULT NULL COMMENT '药品类别',
    `dosage_form` varchar(100) DEFAULT NULL COMMENT '剂型规格',
    `indication` text COMMENT '适应症',
    `manufacturer` varchar(200) DEFAULT NULL COMMENT '生产厂家',
    `supplier` varchar(200) DEFAULT NULL COMMENT '供应商',
    `purchase_price` decimal(12,2) DEFAULT NULL COMMENT '采购价',
    `sale_price` decimal(12,2) DEFAULT NULL COMMENT '销售价',
    `image_url` varchar(500) DEFAULT NULL COMMENT '药品图片',
    `disabled` tinyint(1) DEFAULT '0' COMMENT '是否停用',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_drug_name` (`name`),
    KEY `idx_drug_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品信息';

-- ----------------------------
-- 库存表（药品+批次）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stock` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `drug_id` bigint NOT NULL COMMENT '药品ID',
    `batch_no` varchar(50) DEFAULT NULL COMMENT '批次号',
    `quantity` int NOT NULL DEFAULT '0' COMMENT '数量',
    `production_date` date DEFAULT NULL COMMENT '生产日期',
    `expiry_date` date DEFAULT NULL COMMENT '有效期至',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_drug_batch` (`drug_id`,`batch_no`),
    KEY `idx_stock_drug` (`drug_id`),
    KEY `idx_stock_expiry` (`expiry_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存';

-- ----------------------------
-- 入库记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stock_in` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `drug_id` bigint NOT NULL,
    `batch_no` varchar(50) DEFAULT NULL,
    `quantity` int NOT NULL,
    `production_date` date DEFAULT NULL,
    `expiry_date` date DEFAULT NULL,
    `remark` varchar(500) DEFAULT NULL,
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_stock_in_drug` (`drug_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库记录';

-- ----------------------------
-- 出库记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stock_out` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `drug_id` bigint NOT NULL,
    `batch_no` varchar(50) DEFAULT NULL,
    `quantity` int NOT NULL,
    `remark` varchar(500) DEFAULT NULL,
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_stock_out_drug` (`drug_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库记录';

-- ----------------------------
-- 采购申请
-- ----------------------------
CREATE TABLE IF NOT EXISTS `purchase_request` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `drug_id` bigint NOT NULL,
    `quantity` int NOT NULL,
    `supplier` varchar(200) DEFAULT NULL,
    `budget_amount` decimal(12,2) DEFAULT NULL,
    `reason` varchar(500) DEFAULT NULL COMMENT '采购理由',
    `status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `created_by` bigint DEFAULT NULL,
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    `approved_at` datetime DEFAULT NULL,
    `approved_by` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_purchase_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购申请';

-- ----------------------------
-- 处方表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `prescription` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `patient_name` varchar(50) DEFAULT NULL,
    `doctor_name` varchar(50) DEFAULT NULL,
    `patient_condition` varchar(500) DEFAULT NULL COMMENT '患者病情信息',
    `status` varchar(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/SUBMITTED/APPROVED/REJECTED',
    `review_remark` varchar(500) DEFAULT NULL,
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    `reviewed_at` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方';

-- ----------------------------
-- 处方明细
-- ----------------------------
CREATE TABLE IF NOT EXISTS `prescription_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `prescription_id` bigint NOT NULL,
    `drug_id` bigint NOT NULL,
    `dosage` varchar(100) DEFAULT NULL,
    `route` varchar(50) DEFAULT NULL COMMENT '给药途径',
    `duration` varchar(50) DEFAULT NULL COMMENT '用药时长',
    PRIMARY KEY (`id`),
    KEY `idx_pi_prescription` (`prescription_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方明细';

-- ----------------------------
-- 系统用户
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `username` varchar(50) NOT NULL,
    `password` varchar(200) NOT NULL,
    `real_name` varchar(50) DEFAULT NULL,
    `role` varchar(20) DEFAULT NULL COMMENT 'ADMIN/PHARMACIST/DOCTOR等',
    `enabled` tinyint(1) DEFAULT '1',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

-- ----------------------------
-- 操作日志
-- ----------------------------
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint DEFAULT NULL,
    `module` varchar(50) DEFAULT NULL,
    `action` varchar(100) DEFAULT NULL,
    `detail` text,
    `ip` varchar(50) DEFAULT NULL,
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_log_user` (`user_id`),
    KEY `idx_log_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志';

-- 初始管理员由应用启动时 InitialAdminRunner 创建（用户名 admin，密码 123456），此处不再插入，避免 BCrypt 密文与运行环境不一致导致无法登录。

SET FOREIGN_KEY_CHECKS = 1;
