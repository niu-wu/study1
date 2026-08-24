# study2 招聘业务迁移到 study1-1 设计说明

## 目标

以 `study1-1` 为唯一运行项目，在不破坏现有用户、注册、登录、Token、Redis 会话和修改密码功能的前提下，重新实现 `study2` 招聘任务清单中的后端业务。`study2` 的账号表和登录逻辑不迁移；招聘字段、流程约束和已完成的 Session 1 数据设计作为业务来源。

## 范围

本次纳入：

- Session 1 的目标结果：在 `study11` 中创建招聘数据表，不迁移或覆盖 `study11.user`。
- Session 2-31 的后端功能：招聘基础 CRUD、分页筛选、状态机、简历、复试、录用通知、淘汰/人才库、入职、集成测试、API 文档。
- 每个新增或修改接口的单元/API 测试及一次 Apifox 验证。

本次不纳入：

- study2 的 `user` 表、`User` 实体、账号 CRUD、登录注册和迁移脚本。
- 任何前端页面、组件、样式、构建配置和客户端状态管理。
- 未经单独确认的角色权限扩展、真实 SMTP 发送、OSS 和 Excel 导出。

## 现有架构约束

`study1-1` 的基础包为 `com.example.study11`，使用 Spring Boot 4.1.0、Java 17、MyBatis 4.1.0、MySQL、Redis、BCrypt、Bean Validation 和邮件组件。持久层 Mapper 通过 `MyBatisConfig` 扫描 `com.example.study11.dao`，新增 DAO 必须放在该包路径下。

现有登录流程为：`/sso/login` 查询 `study11.user`，生成 AES Token，并在 Redis 保存 `user:{userId}` 会话；`TokenInterceptor` 从 `x-token` 解析并校验当前用户 ID。招聘接口默认受该拦截器保护，操作人身份只能从 `CURRENT_USER_ID_ATTRIBUTE` 读取，不能信任请求体或 URL 中的 operator/userId。

代码遵守阿里 Java 开发手册的项目化约束：类名和方法名使用 UpperCamelCase/lowerCamelCase，常量全大写下划线，禁止魔法值和超长方法；Controller 只做参数接收和响应转换，Service 承担业务规则和事务，DAO 只负责 SQL；敏感信息不回显，异常使用统一 `ApiException`，日志不记录密码、Token、文件内容；数据库字段和 SQL 使用明确的反引号及索引，事务边界放在 Service。

## 数据设计

数据库目标为 `study11`。不执行 study2 的 `V1__split_user_and_recruitment.sql`，不重命名或删除 `study11.user`。

### recruitment_info

沿用 study2 的招聘业务字段：

- `record_uuid CHAR(36)`：主键，所有招聘流程的业务关联键。
- `id BIGINT UNSIGNED AUTO_INCREMENT`：唯一索引，非主键，仅供数据库编号和排序，不作为流程身份。
- `applicant_name`、`gender`、`position`、`phone`、`email`。
- `application_channel`、`application_method`、`status`。
- `initial_contact_person`、`initial_interview_time`、`retest_contact_person`、`retest_interview_time`。
- `created_at`、`updated_at`。

新增流程表均使用 `record_uuid` 关联招聘记录，不使用 `recruitment_info.id` 作为外键。

### 后续表

按版本迁移新增：

1. `recruitment_status_history`：状态来源、目标、动作、操作人 `user.id`、备注和时间。
2. `candidate_resume`：招聘记录 UUID、原文件名、随机存储名、大小、类型、存储路径、上传人和时间。
3. `retest_application`、`retest_review`：复试申请、评价、招聘类型和领导确认信息。
4. `offer_notice`：通知草稿、收件地址、发送状态和发送时间。
5. `recruitment_rejection`：淘汰阶段、原因、备注、人才库分类和操作人。
6. `onboarding_record`：招聘记录 UUID、创建的 `user.id`、报到信息和入职时间。

外键约束优先引用 `recruitment_info.record_uuid` 和 `user.id`；删除策略使用受控业务删除，避免级联删除账号或招聘历史。

## 领域与接口设计

招聘模块按 `study1-1` 的分层实现：

```text
com.example.study11.recruitment
├── controller
├── dao
├── entity/dto
├── entity/po
├── entity/vo
├── service
└── service/impl
```

基础接口保留 study2 的 URL 语义，但使用 study1-1 的响应和异常风格：

- `POST /api/recruitment-info`
- `GET /api/recruitment-info`
- `GET /api/recruitment-info/{recordUuid}`
- `GET /api/recruitment-info/by-id/{id}`
- `PUT /api/recruitment-info/{recordUuid}`
- `DELETE /api/recruitment-info/{recordUuid}`
- `GET /api/recruitment-info/page`
- `GET /api/recruitment-info/statistics`

后续动作接口全部登录后调用，并在 Service 中从 Token 对应的请求属性获取操作人：

- `POST /api/retest/apply`
- `POST /api/retest/confirm`
- `POST /api/offers/draft`
- `POST /api/offers/send`
- `POST /api/rejections/reject`
- `POST /api/rejections/decline`
- `POST /api/onboarding/process`
- `POST /api/resumes/upload`
- `GET /api/resumes/{id}`
- `GET /api/resumes/download/{id}`
- `GET /api/resumes/list/{recordUuid}`

创建和更新请求使用 DTO，禁止客户端设置 `id`、`recordUuid`、`legacyUserId`、系统状态和操作人。响应使用 VO，禁止返回密码、内部文件路径和敏感认证字段。

## 状态机

状态值保存在数据库中，第一阶段使用英文枚举：

```text
PENDING_INITIAL -> RETEST_REVIEW -> PENDING_RETEST -> PENDING_ONBOARDING -> ONBOARDED
PENDING_INITIAL/RETEST_REVIEW/PENDING_RETEST/PENDING_ONBOARDING -> REJECTED
PENDING_INITIAL/RETEST_REVIEW/PENDING_RETEST/PENDING_ONBOARDING -> DECLINED
```

禁止基础更新接口直接修改 `status`。所有状态变化必须由动作 Service 校验当前状态、更新记录并写入状态历史，且在同一事务中完成。

## Apifox 验证流程

建立 Apifox 项目 `study1-1 招聘管理后端`，环境变量至少包含 `baseUrl`、`token`、`recordUuid`、`recruitmentId`、`userId`。登录请求将返回 Token 写入 `token`，受保护接口统一发送 `x-token: {{token}}`。

每个接口 Session 必须按以下顺序完成：

1. 先写自动化测试并确认失败或覆盖缺失行为。
2. 实现最小代码并运行该 Session 测试。
3. 启动 `study1-1`，在 Apifox 调用刚完成的接口。
4. 保存请求、响应、状态码、数据库关键结果和异常场景。
5. 运行全量 Maven 测试后，才进入下一个 Session。

## 兼容性与风险控制

- 不修改现有 `UserPo`、`UserDao`、`SsoServiceImpl`、`TokenInterceptor` 的数据库语义。
- 不将 study2 的 `ApiExceptionHandler`、`UserService` 或 MyBatis 配置复制到 study1-1。
- 数据迁移只创建 `study11` 新表；study2 数据库可弃用，不作为运行时依赖。
- 若需 Flyway，使用 study1-1 自己的版本目录和数据库连接，默认关闭启动自动迁移，迁移通过受控命令执行。
- Apifox 只验证 HTTP 接口，不代替 Maven 测试和数据库结构测试。

