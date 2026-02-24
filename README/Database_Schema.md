# 数据库表结构说明

本项目使用 MySQL 数据库，主要包含以下 9 张核心业务表：

## 1. 用户与权限相关

### `user` (用户表)
存储系统所有用户（学生、场地管理员、系统管理员）的基本信息。
- `id`: 主键
- `username`: 学号/工号 (唯一键)
- `real_name`: 姓名
- `password`: 密码 (默认 123456)
- `role`: 角色 (`STUDENT`, `VENUE_ADMIN`, `SYS_ADMIN`)
- `status`: 状态 (1:正常 0:禁用)
- `credit_score`: 信用分 (默认 100)
- `signature_url`: 电子签名图片地址

### `venue_admin_building` (场地管理员管辖楼宇关联表)
多对多关联表，用于记录哪个场地管理员负责管理哪些楼宇。
- `id`: 主键
- `user_id`: 管理员ID (关联 `user.id`)
- `building_id`: 楼宇ID (关联 `building.id`)

## 2. 场地资源相关

### `building` (楼宇/楼栋表)
存储学校内的楼宇信息（如：存中楼、博易楼）。
- `id`: 主键
- `name`: 楼宇名称
- `location`: 所在区域
- `description`: 描述

### `venue` (场地表)
存储具体的场地/房间信息，归属于某个楼宇。
- `id`: 主键
- `name`: 场地名称 (如：201会议室)
- `building_id`: 所属楼宇ID (关联 `building.id`)
- `type`: 场地类型 (如：会议室、体育馆、教室)
- `capacity`: 容纳人数
- `equipment`: 设备清单
- `status`: 状态 (1:开放 0:关闭)

### `venue_lock` (场地锁定/维护时间段表)
用于记录场地被管理员手动锁定或维护的时间段，该时间段内不可预约。
- `id`: 主键
- `venue_id`: 场地ID
- `start_time`: 锁定开始时间
- `end_time`: 锁定结束时间
- `reason`: 锁定原因

## 3. 业务流转相关

### `booking` (预约表)
核心业务表，记录用户的场地预约申请及审批状态。
- `id`: 主键
- `user_id`: 预约用户ID
- `venue_id`: 预约场地ID
- `start_time`: 预约开始时间
- `end_time`: 预约结束时间
- `status`: 状态 (`PENDING`:待审批, `APPROVED`:已通过, `REJECTED`:已驳回, `CANCELLED`:已取消)
- `remark`: 活动内容/备注
- `plan_url`: 策划书附件URL
- `reject_reason`: 驳回原因

### `credit_log` (信用日志表)
记录用户信用分的变动历史（如：违约扣分、履约加分）。
- `id`: 主键
- `user_id`: 用户ID
- `change_value`: 变动值 (正数或负数)
- `reason`: 变动原因

## 4. 消息与通知相关

### `notification` (通知表)
系统发送给个人的站内信/通知。
- `id`: 主键
- `user_id`: 接收用户ID
- `title`: 通知标题
- `content`: 通知内容
- `type`: 类型 (`SYSTEM`, `BOOKING`, `PERSONAL`)
- `is_read`: 是否已读 (0:未读, 1:已读)

### `announcement` (公告表)
系统管理员发布的全局公告。
- `id`: 主键
- `title`: 公告标题
- `content`: 公告内容
- `type`: 类型 (`NORMAL`:普通, `URGENT`:紧急)
- `published_by`: 发布人ID (关联 `user.id`)
