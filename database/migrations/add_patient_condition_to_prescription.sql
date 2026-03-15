-- 为处方表增加「患者病情信息」字段（已有库执行此脚本，若已存在该列可忽略报错）
USE smart_pharma;
ALTER TABLE `prescription` ADD COLUMN `patient_condition` varchar(500) DEFAULT NULL COMMENT '患者病情信息' AFTER `doctor_name`;
