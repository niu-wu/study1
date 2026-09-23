-- V18: 应聘者小程序公司简介（全公司只有一条记录）

CREATE TABLE IF NOT EXISTS `mini_company_profile`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增编号',
    `title`         VARCHAR(100) NOT NULL DEFAULT '关于我们' COMMENT '页面标题',
    `content`       MEDIUMTEXT NOT NULL COMMENT '公司介绍正文',
    `stat1_value`   VARCHAR(20) DEFAULT NULL COMMENT '数据卡1数值',
    `stat1_label`   VARCHAR(50) DEFAULT NULL COMMENT '数据卡1说明',
    `stat2_value`   VARCHAR(20) DEFAULT NULL COMMENT '数据卡2数值',
    `stat2_label`   VARCHAR(50) DEFAULT NULL COMMENT '数据卡2说明',
    `stat3_value`   VARCHAR(20) DEFAULT NULL COMMENT '数据卡3数值',
    `stat3_label`   VARCHAR(50) DEFAULT NULL COMMENT '数据卡3说明',
    `created_by`    INT DEFAULT NULL COMMENT '创建人',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`    INT DEFAULT NULL COMMENT '最后修改人',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted`       TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公司简介';

-- 插入初始数据
INSERT INTO `mini_company_profile`
(title, content, stat1_value, stat1_label, stat2_value, stat2_label, stat3_value, stat3_label)
VALUES
('关于我们',
'济南北海软件工程有限公司成立于2010年10月，现有员工120余人，位于济南高新区齐鲁软件园商圈，历经十余年的发展，目前公司业务范围涵盖对日软件外包、国内软件项目外包、定制开发、人才派遣、软件人才培养等诸多领域，现已在日本东京设立分支机构。公司先后获得了双软企业、高新技术企业、ISO9001质量认证、ISO27001信息安全管理体系认证质、商务诚信3A级企业等资质。公司自成立以来先后于NEC、日立、松下、国家电网、中国电信、浙江宇视、水发集团、浪潮、普联、中铁、山东高速等世界500强企业以及国内知名企业建立了深度合作。为企业带来数字化价值应用，推动企业完成数字化、信息化管理升级。',
'12', '年服务经验',
'2000+', '客户选择',
'100+', '精英团队');
