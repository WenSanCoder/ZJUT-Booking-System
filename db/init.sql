CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `username` varchar(64) NOT NULL COMMENT '学号/工号',
                        `real_name` varchar(64) NOT NULL COMMENT '姓名',
                        `password` varchar(128) NOT NULL DEFAULT '123456' COMMENT '密码',
                        `role` varchar(20) NOT NULL COMMENT '角色 STUDENT, VENUE_ADMIN, SYS_ADMIN',
                        `phone` varchar(20) DEFAULT NULL COMMENT '电话',
                        `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
                        `status` int(11) DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
                        `password_changed` tinyint(1) DEFAULT 0 COMMENT '是否已修改初始密码 0:否 1:是',
                        `credit_score` int(11) DEFAULT 100 COMMENT '信用分',
                        `signature_url` varchar(255) DEFAULT NULL COMMENT '电子签名图片地址',
                        `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `building` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT,
                            `name` varchar(64) NOT NULL COMMENT '楼宇名称',
                            `location` varchar(128) DEFAULT NULL COMMENT '所在区域',
                            `description` varchar(255) DEFAULT NULL COMMENT '描述',
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼宇/楼栋表';

CREATE TABLE `venue` (
                         `id` bigint(20) NOT NULL AUTO_INCREMENT,
                         `name` varchar(64) NOT NULL COMMENT '场地名称',
                         `building_id` bigint(20) DEFAULT NULL COMMENT '所属楼宇ID',
                         `type` varchar(32) NOT NULL COMMENT '场地类型',
                         `location` varchar(128) DEFAULT NULL COMMENT '详细位置',
                         `capacity` int(11) DEFAULT NULL COMMENT '容量',
                         `image_url` varchar(255) DEFAULT NULL COMMENT '场地照片URL',
                         `equipment` text DEFAULT NULL COMMENT '设备清单',
                         `status` int(11) DEFAULT 1 COMMENT '状态 1:开放 0:关闭',
                         `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地表';

CREATE TABLE `venue_admin_building` (
                                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                        `user_id` bigint(20) NOT NULL COMMENT '管理员ID',
                                        `building_id` bigint(20) NOT NULL COMMENT '楼宇ID',
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地管理员管辖楼宇关联表';

CREATE TABLE `notification` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                `user_id` bigint(20) NOT NULL COMMENT '接收用户ID',
                                `title` varchar(128) DEFAULT NULL,
                                `content` text NOT NULL COMMENT '通知内容',
                                `type` varchar(20) DEFAULT 'SYSTEM' COMMENT '类型 SYSTEM, BOOKING, PERSONAL',
                                `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读',
                                `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

CREATE TABLE `booking` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                           `venue_id` bigint(20) NOT NULL COMMENT '场地ID',
                           `start_time` datetime NOT NULL COMMENT '开始时间',
                           `end_time` datetime NOT NULL COMMENT '结束时间',
                           `status` varchar(20) NOT NULL COMMENT '状态 PENDING:待审批, APPROVED:已通过, REJECTED:已驳回, CANCELLED:已取消',
                           `remark` text DEFAULT NULL COMMENT '活动内容/备注',
                           `plan_url` varchar(255) DEFAULT NULL COMMENT '策划书URL',
                           `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
                           `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `activity_name` varchar(255) DEFAULT NULL COMMENT '活动名称',
                           `organizer` varchar(255) DEFAULT NULL COMMENT '组织单位',
                           `expected_people` int(11) DEFAULT NULL COMMENT '预计人数',
                           `contact_name` varchar(100) DEFAULT NULL COMMENT '联系人',
                           `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
                           `description` text DEFAULT NULL COMMENT '详情',
                           `attachment` varchar(255) DEFAULT NULL COMMENT '附件URL',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

CREATE TABLE `credit_log` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT,
                              `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                              `change_value` int(11) NOT NULL COMMENT '变动值',
                              `reason` varchar(255) DEFAULT NULL COMMENT '原因',
                              `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用日志表';

CREATE TABLE `venue_lock` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT,
                              `venue_id` bigint(20) NOT NULL COMMENT '场地ID',
                              `start_time` datetime NOT NULL COMMENT '开始时间',
                              `end_time` datetime NOT NULL COMMENT '结束时间',
                              `reason` varchar(255) DEFAULT NULL COMMENT '锁定原因',
                              `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地锁定/维护时间段表';

CREATE TABLE `venue_admin_permission` (
                                          `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                          `user_id` bigint(20) NOT NULL COMMENT '管理员用户ID',
                                          `target_type` varchar(20) NOT NULL COMMENT '权限目标类型: CAMPUS, BUILDING, VENUE',
                                          `target_id` varchar(64) NOT NULL COMMENT '目标ID (校区名, 楼宇ID, 或场地ID)',
                                          `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地管理员权限表';

CREATE TABLE `announcement` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                `title` varchar(128) NOT NULL COMMENT '标题',
                                `content` text NOT NULL COMMENT '内容',
                                `type` varchar(20) DEFAULT 'NORMAL' COMMENT '类型 NORMAL, URGENT',
                                `published_by` bigint(20) DEFAULT NULL COMMENT '发布者ID',
                                `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 插入一些初始楼宇数据
INSERT INTO `building` (`name`, `location`) VALUES 
('存中楼', '朝晖校区'),
('博易楼', '屏峰校区'),
('西侧体育馆', '西侧场馆区');

-- 插入一些初始公告数据
INSERT INTO `announcement` (`title`, `content`, `type`, `published_by`) VALUES 
('系统上线通知', '欢迎使用本校场地预约系统，现已进入试运行阶段。', 'NORMAL', 1),
('场地维护公告', '本周末西侧体育馆将进行地面维护，暂停开放。', 'URGENT', 1);

-- 插入一些初始场地数据
INSERT INTO `venue` (`name`, `building_id`, `type`, `location`, `capacity`, `equipment`) VALUES
('存中楼二楼会议室', 1, '会议室', '201室', 50, '投影仪, 麦克风'),
('板球馆主场', 3, '体育馆', '1层', 200, '计分板'),
('博易楼 101', 2, '教室', '1层', 80, '投影仪');

-- 分配场地管理员管辖范围 (工号20001 管理 存中楼)
INSERT INTO `venue_admin_building` (`user_id`, `building_id`) VALUES (2, 1);
-- 插入测试用户数据 (默认密码均为 123456)
INSERT INTO `user` (`username`, `real_name`, `role`, `phone`, `email`) VALUES
                                                                           ('admin001', '系统管理员', 'SYS_ADMIN', '13800000001', 'admin@zjut.edu.cn'),
                                                                           ('manager001', '场地管理员', 'VENUE_ADMIN', '13800000002', 'manager@zjut.edu.cn');