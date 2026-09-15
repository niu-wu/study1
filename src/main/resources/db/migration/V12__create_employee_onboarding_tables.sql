-- Employee onboarding registration tables.
-- WeChat binding stays on employee; do not create a separate bind or attachment table.

CREATE TABLE IF NOT EXISTS `employee`
(
    `employee_uuid`         CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自动编号，非主键',
    `employee_no`           VARCHAR(20) NOT NULL COMMENT '工号，系统生成',
    `user_id`               INT NOT NULL COMMENT '关联的系统用户编号',
    `record_uuid`           CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '入职办理对应的招聘记录',
    `full_name`             VARCHAR(50) NOT NULL COMMENT '姓名，预填后可随草稿保存',
    `gender`                VARCHAR(10) DEFAULT NULL COMMENT '性别',
    `birth_date`            DATE DEFAULT NULL COMMENT '出生日期',
    `position`              VARCHAR(100) DEFAULT NULL COMMENT '职位快照，来自招聘记录',
    `phone`                 VARCHAR(20) DEFAULT NULL COMMENT '手机号，预填自招聘记录',
    `email`                 VARCHAR(100) DEFAULT NULL COMMENT '邮箱，预填自招聘记录',
    `id_card`               VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `marital_status`        VARCHAR(20) DEFAULT NULL COMMENT '婚姻状况',
    `political_status`      VARCHAR(30) DEFAULT NULL COMMENT '政治面貌',
    `nationality`           VARCHAR(50) DEFAULT NULL COMMENT '国籍',
    `ethnicity`             VARCHAR(30) DEFAULT NULL COMMENT '民族',
    `native_place`          VARCHAR(100) DEFAULT NULL COMMENT '籍贯',
    `hukou_location`        VARCHAR(200) DEFAULT NULL COMMENT '户口所在地，页面重复展示时仍只存一列',
    `current_address`       VARCHAR(200) DEFAULT NULL COMMENT '现住地址，页面重复展示时仍只存一列',
    `postal_code`           VARCHAR(10) DEFAULT NULL COMMENT '邮编',
    `health_status`         VARCHAR(50) DEFAULT NULL COMMENT '健康状况',
    `highest_education`     VARCHAR(50) DEFAULT NULL COMMENT '最高学历',
    `major`                 VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `professional_title`    VARCHAR(100) DEFAULT NULL COMMENT '专业职称',
    `foreign_language`      VARCHAR(100) DEFAULT NULL COMMENT '外语及等级',
    `hobbies`               VARCHAR(200) DEFAULT NULL COMMENT '爱好特长',
    `photo_path`            VARCHAR(500) DEFAULT NULL COMMENT '一寸照片存储路径',
    `wechat_account`        VARCHAR(64) DEFAULT NULL COMMENT '微信号',
    `wechat_openid`         VARCHAR(64) DEFAULT NULL COMMENT '微信 OpenID，扫码绑定后写入',
    `wechat_unionid`        VARCHAR(64) DEFAULT NULL COMMENT '微信 UnionID，扫码绑定后写入',
    `wechat_bound_at`       DATETIME DEFAULT NULL COMMENT '微信绑定时间',
    `form_status`           VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '登记表状态：DRAFT、SUBMITTED',
    `submitted_at`          DATETIME DEFAULT NULL COMMENT '提交锁定时间',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`employee_uuid`),
    UNIQUE KEY `uk_employee_id` (`id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`),
    UNIQUE KEY `uk_employee_user_id` (`user_id`),
    UNIQUE KEY `uk_employee_record_uuid` (`record_uuid`),
    UNIQUE KEY `uk_employee_id_card` (`id_card`),
    UNIQUE KEY `uk_employee_wechat_openid` (`wechat_openid`),
    KEY `idx_employee_form_status` (`form_status`),
    CONSTRAINT `fk_employee_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_employee_onboarding`
        FOREIGN KEY (`record_uuid`) REFERENCES `onboarding_record` (`record_uuid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工入职登记主档';

CREATE TABLE IF NOT EXISTS `employee_education`
(
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '学历记录编号',
    `employee_uuid`         CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `sort_no`               INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    `start_date`            DATE DEFAULT NULL COMMENT '起始日期',
    `end_date`              DATE DEFAULT NULL COMMENT '结束日期',
    `school_name`           VARCHAR(100) DEFAULT NULL COMMENT '院校名称',
    `major`                 VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `education_level`       VARCHAR(50) DEFAULT NULL COMMENT '学历',
    `certificate`           VARCHAR(200) DEFAULT NULL COMMENT '证书',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_employee_education_employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_education_employee`
        FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工学历经历';

CREATE TABLE IF NOT EXISTS `employee_work_history`
(
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '工作经历编号',
    `employee_uuid`         CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `sort_no`               INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    `start_date`            DATE DEFAULT NULL COMMENT '起始日期',
    `end_date`              DATE DEFAULT NULL COMMENT '结束日期',
    `company_name`          VARCHAR(100) DEFAULT NULL COMMENT '工作单位',
    `position`              VARCHAR(100) DEFAULT NULL COMMENT '职位',
    `leave_reason`          VARCHAR(200) DEFAULT NULL COMMENT '离职原因',
    `reference_name`        VARCHAR(50) DEFAULT NULL COMMENT '证明人姓名',
    `reference_phone`       VARCHAR(20) DEFAULT NULL COMMENT '证明人电话',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_employee_work_history_employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_work_history_employee`
        FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工工作经历';

CREATE TABLE IF NOT EXISTS `employee_training`
(
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '培训经历编号',
    `employee_uuid`         CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `sort_no`               INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    `start_date`            DATE DEFAULT NULL COMMENT '起始日期',
    `end_date`              DATE DEFAULT NULL COMMENT '结束日期',
    `institution`           VARCHAR(100) DEFAULT NULL COMMENT '培训机构',
    `course_content`        VARCHAR(200) DEFAULT NULL COMMENT '培训内容',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_employee_training_employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_training_employee`
        FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工培训经历';

CREATE TABLE IF NOT EXISTS `employee_family`
(
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '家庭成员编号',
    `employee_uuid`         CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `sort_no`               INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    `full_name`             VARCHAR(50) DEFAULT NULL COMMENT '姓名',
    `relationship`          VARCHAR(30) DEFAULT NULL COMMENT '关系',
    `work_unit`             VARCHAR(100) DEFAULT NULL COMMENT '工作单位',
    `job_title`             VARCHAR(100) DEFAULT NULL COMMENT '所任岗位及职务',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_employee_family_employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_family_employee`
        FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工家庭成员';

CREATE TABLE IF NOT EXISTS `employee_emergency_contact`
(
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '紧急联络人编号',
    `employee_uuid`         CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `sort_no`               INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    `full_name`             VARCHAR(50) DEFAULT NULL COMMENT '姓名',
    `relationship`          VARCHAR(30) DEFAULT NULL COMMENT '关系',
    `address`               VARCHAR(200) DEFAULT NULL COMMENT '联系地址',
    `postal_code`           VARCHAR(10) DEFAULT NULL COMMENT '邮编',
    `phone`                 VARCHAR(20) DEFAULT NULL COMMENT '电话',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_employee_emergency_contact_employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_emergency_contact_employee`
        FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工紧急联络人';
