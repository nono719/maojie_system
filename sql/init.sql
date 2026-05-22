-- =====================================================================
--  BreathChain - 基于区块链的呼吸训练干预系统
--  数据库初始化脚本 (MySQL 8.0)
--  对应论文 5.2.2 与附录 B
-- =====================================================================

CREATE DATABASE IF NOT EXISTS breathchain
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE breathchain;

-- ---------------------------------------------------------------------
-- 1. 用户表 sys_user (论文 表5-1)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(50)     NOT NULL                COMMENT '登录账号',
    password        VARCHAR(255)    NOT NULL                COMMENT 'BCrypt 加密密码',
    real_name       VARCHAR(50)     DEFAULT NULL            COMMENT '真实姓名',
    phone           VARCHAR(20)     DEFAULT NULL            COMMENT '手机号',
    email           VARCHAR(100)    DEFAULT NULL            COMMENT '邮箱',
    avatar          VARCHAR(255)    DEFAULT NULL            COMMENT '头像URL',
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/DOCTOR/USER',
    doctor_id       BIGINT          DEFAULT NULL            COMMENT '患者绑定的医生ID',
    wallet_address  VARCHAR(64)     DEFAULT NULL            COMMENT '区块链钱包地址',
    status          TINYINT         NOT NULL DEFAULT 1      COMMENT '状态: 1=启用 0=停用',
    deleted         TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_doctor_id (doctor_id),
    KEY idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------------------------------------------------------------------
-- 2. 医生扩展信息表 doctor_profile
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS doctor_profile;
CREATE TABLE doctor_profile (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL                COMMENT '关联 sys_user.id',
    license_no      VARCHAR(64)     NOT NULL                COMMENT '执业医师资格证书编号',
    hospital        VARCHAR(100)    DEFAULT NULL            COMMENT '所属医院',
    department      VARCHAR(50)     DEFAULT NULL            COMMENT '科室',
    title           VARCHAR(50)     DEFAULT NULL            COMMENT '职称',
    certified       TINYINT         NOT NULL DEFAULT 0      COMMENT '是否通过认证',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_license (license_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生资质信息表';

-- ---------------------------------------------------------------------
-- 3. 训练任务表 breathing_task (论文 表5-2)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS breathing_task;
CREATE TABLE breathing_task (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    task_name       VARCHAR(100)    NOT NULL                COMMENT '任务名称',
    description     VARCHAR(500)    DEFAULT NULL            COMMENT '任务描述',
    inhale_seconds  INT             NOT NULL DEFAULT 4      COMMENT '吸气时长(秒)',
    hold_seconds    INT             NOT NULL DEFAULT 2      COMMENT '屏息时长(秒)',
    exhale_seconds  INT             NOT NULL DEFAULT 6      COMMENT '呼气时长(秒)',
    keep_seconds    INT             NOT NULL DEFAULT 2      COMMENT '保持时长(秒)',
    duration        INT             NOT NULL DEFAULT 300    COMMENT '单次训练总时长(秒)',
    daily_times     INT             NOT NULL DEFAULT 3      COMMENT '每天训练次数',
    reward_amount   DECIMAL(18,6)   NOT NULL DEFAULT 10.0   COMMENT '奖励代币数量',
    doctor_id       BIGINT          NOT NULL                COMMENT '创建医生ID',
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/ARCHIVED',
    deleted         TINYINT         NOT NULL DEFAULT 0,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_doctor_id (doctor_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='呼吸训练任务表';

-- ---------------------------------------------------------------------
-- 4. 任务分配表 task_assignment
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS task_assignment;
CREATE TABLE task_assignment (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    task_id         BIGINT          NOT NULL                COMMENT '任务ID',
    user_id         BIGINT          NOT NULL                COMMENT '患者ID',
    doctor_id       BIGINT          NOT NULL                COMMENT '分配医生ID',
    start_date      DATE            DEFAULT NULL            COMMENT '开始日期',
    end_date        DATE            DEFAULT NULL            COMMENT '结束日期',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/PAUSED/FINISHED',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_user (task_id, user_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分配表';

-- ---------------------------------------------------------------------
-- 5. 训练记录表 training_record (论文 表5-3)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS training_record;
CREATE TABLE training_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '记录ID(对应链上recordId)',
    user_id         BIGINT          NOT NULL                COMMENT '用户ID',
    task_id         BIGINT          NOT NULL                COMMENT '任务ID',
    duration        INT             NOT NULL                COMMENT '实际训练时长(秒)',
    breath_count    INT             DEFAULT NULL            COMMENT '呼吸次数',
    completion_rate DECIMAL(5,2)    NOT NULL                COMMENT '完成率(%)',
    score           INT             NOT NULL                COMMENT '训练评分(0-100)',
    heart_rate      INT             DEFAULT NULL            COMMENT '平均心率',
    data_hash       VARCHAR(66)     DEFAULT NULL            COMMENT '链下数据 Keccak-256 哈希 (0x...)',
    block_tx_id     VARCHAR(100)    DEFAULT NULL            COMMENT '区块链交易哈希',
    chain_status    VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '上链状态: PENDING/SUCCESS/FAILED',
    chain_error     VARCHAR(500)    DEFAULT NULL            COMMENT '上链失败原因',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_task (user_id, task_id),
    KEY idx_create_time (create_time),
    KEY idx_chain_status (chain_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练记录表';

-- ---------------------------------------------------------------------
-- 6. 奖励记录表 reward_record
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS reward_record;
CREATE TABLE reward_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL                COMMENT '受奖用户ID',
    task_id         BIGINT          NOT NULL                COMMENT '任务ID',
    training_record_id BIGINT       NOT NULL                COMMENT '对应训练记录ID',
    amount          DECIMAL(18,6)   NOT NULL                COMMENT '奖励数量',
    tx_hash         VARCHAR(100)    DEFAULT NULL            COMMENT '区块链交易哈希',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_training (training_record_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖励发放记录';

-- ---------------------------------------------------------------------
-- 7. 用户代币余额缓存表 user_token_balance
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS user_token_balance;
CREATE TABLE user_token_balance (
    user_id         BIGINT          NOT NULL                COMMENT '用户ID',
    wallet_address  VARCHAR(64)     NOT NULL                COMMENT '钱包地址',
    balance         DECIMAL(36,6)   NOT NULL DEFAULT 0      COMMENT '链上余额（缓存）',
    last_sync_time  DATETIME        DEFAULT NULL            COMMENT '上次与链同步时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    KEY idx_wallet (wallet_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户代币余额缓存';

-- ---------------------------------------------------------------------
-- 8. 操作审计日志 sys_audit_log (论文 4.5 非功能要求)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS sys_audit_log;
CREATE TABLE sys_audit_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          DEFAULT NULL,
    username        VARCHAR(50)     DEFAULT NULL,
    action          VARCHAR(50)     NOT NULL                COMMENT '操作类型',
    resource        VARCHAR(100)    DEFAULT NULL            COMMENT '资源',
    method          VARCHAR(10)     DEFAULT NULL,
    uri             VARCHAR(255)    DEFAULT NULL,
    ip              VARCHAR(64)     DEFAULT NULL,
    result          VARCHAR(20)     DEFAULT NULL            COMMENT 'SUCCESS/FAILED',
    detail          TEXT            DEFAULT NULL,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志';

-- =====================================================================
--  初始化数据
-- =====================================================================

-- 默认管理员账户  username: admin   password: admin123 (BCrypt)
INSERT INTO sys_user (username, password, real_name, role)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 'ADMIN');

-- 示例医生（密码同上）
INSERT INTO sys_user (username, password, real_name, phone, role)
VALUES ('doctor01', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '金虎', '13800138001', 'DOCTOR');

INSERT INTO doctor_profile (user_id, license_no, hospital, department, title, certified)
VALUES (LAST_INSERT_ID(), 'CD-2024-0001', '成都信息工程大学附属医院', '呼吸科', '主任医师', 1);

-- 示例患者
INSERT INTO sys_user (username, password, real_name, phone, role, doctor_id)
VALUES ('patient01', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张三', '13800138002', 'USER', 2);

-- 示例训练任务
INSERT INTO breathing_task
  (task_name, description, inhale_seconds, hold_seconds, exhale_seconds, keep_seconds, duration, daily_times, reward_amount, doctor_id, status)
VALUES
  ('腹式呼吸基础训练', '适用于慢阻肺患者居家康复 - 478呼吸法变体', 4, 7, 8, 0, 600, 3, 10.000000, 2, 'PUBLISHED'),
  ('心脏康复呼吸训练', '心脏术后或心衰患者康复用', 4, 2, 6, 2, 480, 2, 8.000000, 2, 'PUBLISHED'),
  ('焦虑缓解放松训练', '焦虑症辅助治疗', 5, 0, 7, 0, 300, 4, 5.000000, 2, 'PUBLISHED');

INSERT INTO task_assignment (task_id, user_id, doctor_id, start_date, status)
VALUES (1, 3, 2, CURDATE(), 'ACTIVE');
