# study1-1 招聘后端迁移实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在保留 study1-1 用户与登录体系的前提下，完成 study2 招聘后端任务在 study1-1 中的重写、数据库建表、接口测试和 Apifox 验证。

**Architecture:** 以 `com.example.study11` 为根包，招聘模块遵守现有 Controller- Service- DAO- PO/DTO/VO 分层；所有招聘流程以 `recruitment_info.record_uuid` 为业务关联键，账号关联只引用现有 `user.id`。每个接口先自动化测试，再启动应用使用 Apifox 验证。

**Tech Stack:** Java 17, Spring Boot 4.1.0, MyBatis 4.1.0, MySQL, Redis, BCrypt, Bean Validation, JUnit 5, Mockito, MockMvc, Flyway only if added to study1-1 after dependency review.

---

## 执行规则

- 只在 `D:\Java\study1-1` 的 `dev` 分支修改。
- 每个 Session 先写测试，运行一次确认测试因功能缺失而失败，再写最小实现，最后运行该 Session 测试和全量测试。
- 每个 HTTP 接口完成后，在 Apifox 项目 `study1-1 招聘管理后端` 中执行成功和至少一个失败场景，并保存 `baseUrl`、`token`、`recordUuid`、`recruitmentId`、`userId` 环境变量。
- 每项工作完成后，删除后续不再需要的临时 PNG/截图；仅保留仍作为验收证据或项目资源明确需要的图片，临时图片不得进入提交。
- Controller 不写业务规则；Service 负责校验、事务和状态变化；DAO 只负责 MyBatis SQL；PO 不直接作为请求体或响应体。
- 不复制 study2 的 `user`、登录、注册、异常处理器和数据库迁移脚本。
- 不返回密码哈希、Token、服务器文件路径或初始密码的持久化值；入职接口的初始密码只允许一次性响应。

## 文件边界

### 现有文件只做必要修改

- `pom.xml`：仅补充数据库迁移或测试所需且不存在的依赖。
- `src/main/resources/application.yml`：仅增加招聘表迁移、分页和文件存储配置，保留现有数据源、Redis、SSO、邮件配置。
- `src/main/java/com/example/study11/config/MyBatisConfig.java`：只在需要时扩展 Mapper 扫描，优先保持现有 `dao` 路径。
- `src/main/java/com/example/study11/exception/GlobalExceptionHandler.java`：补充招聘领域异常映射，不替换现有错误结构。
- `src/main/java/com/example/study11/filter/InterceptorConfig.java`：默认不增加白名单；招聘接口必须登录。

### 新增模块文件

招聘模块遵循现有 `study11` 分层目录：

```text
src/main/java/com/example/study11/controller/
src/main/java/com/example/study11/dao/
src/main/java/com/example/study11/entity/dto/
src/main/java/com/example/study11/entity/enums/
src/main/java/com/example/study11/entity/po/
src/main/java/com/example/study11/entity/vo/
src/main/java/com/example/study11/service/
src/main/java/com/example/study11/service/impl/
src/main/java/com/example/study11/common/model/
```

`common/model/` 保留共享分页模型。Mapper XML 放在 `src/main/resources/mapper/`，命名与 DAO 接口一一对应。数据库版本文件放在 `src/main/resources/db/migration/`，具体是否引入 Flyway在 Session 1 依赖审查时确定。

## Session 1：study11 招聘基础表

**依赖：** 现有 `study11.user` 已存在；执行前必须备份 `study11`。

**文件：** 创建 `src/main/resources/db/migration/V1__create_recruitment_info.sql` 或项目确认的等价受控 SQL；创建 `src/test/resources/db/recruitment_schema.sql`；修改迁移配置仅限必要项。

- [x] 写数据库结构测试，验证 `recruitment_info.record_uuid` 为主键、`id` 为 `AUTO_INCREMENT` 且不是主键、招聘字段齐全，且 `user` 表列集合不变。
- [x] 运行结构测试确认缺少新表时失败。
- [x] 创建 `recruitment_info`，`record_uuid` 使用 `CHAR(36)` 主键，`id` 使用 `BIGINT UNSIGNED AUTO_INCREMENT` 加唯一索引，所有旧招聘字段按 study2 类型保留。
- [x] 在测试库执行迁移，检查外键、索引、默认状态和时间字段。
- [x] 执行 `./mvnw.cmd test`，备份后在 `study11` 验证 `SHOW CREATE TABLE` 和 `information_schema`。
- [x] 在 Apifox 创建项目和环境，项目和环境记录见 `docs/recruitment-backend-api.md`。

## Session 2-4：现有用户体系回归与接口边界

**依赖：** Session 1；不迁移 study2 User。

- [x] 为现有 `/sso/register`、`/sso/login`、`/users` 和 `/users/me/password` 补充回归测试，确认数据库仍使用 `study11.user`。
- [x] 增加测试确认招聘接口没有被加入登录白名单，缺少 `x-token` 返回 401。
- [x] 检查现有用户创建、登录和改密路径的 BCrypt、敏感字段不回显、用户名冲突和统一异常行为；只修复会阻塞招聘模块的缺陷。
- [x] 在 Apifox 验证登录并把返回 Token 保存为 `token` 环境变量；验证一个受保护的既有接口，记录响应。

## Session 5：招聘 DTO、PO、DAO、Service、Controller

**依赖：** Session 1-4。

**文件：** 新建招聘分层文件和 `RecruitmentInfoMapper.xml`；不修改用户 DAO。

- [x] 写 Service 和 MockMvc 测试：创建忽略客户端 `id/status/recordUuid`，更新不允许修改 `status`，必填姓名和岗位校验，未登录返回 401。
- [x] 运行测试确认招聘模块类和接口缺失导致失败。
- [x] 创建招聘 PO、DTO、VO、DAO、Service 和 Controller。
- [x] DAO SQL 只访问 `recruitment_info`，插入时不写 `id`，使用 `useGeneratedKeys` 回填数据库编号；更新和删除按 `record_uuid`。
- [x] Controller 使用校验 DTO，接口返回 `ResponseEntity`，异常交给 `GlobalExceptionHandler`。
- [x] 运行 Session 测试和全量 Maven 测试；启动服务后在 Apifox 验证基础 CRUD，并保存 `recordUuid` 和 `recruitmentId`。

## Session 6：基础 CRUD 接口回归

**依赖：** Session 5。

- [x] 增加 Controller 测试覆盖成功状态码、404、400、非法 UUID、不可修改状态和 Mapper 不引用 `user`。
- [x] 运行测试确认新断言先失败，再补齐响应和异常处理。
- [x] 在 Apifox 对每个 CRUD 接口执行成功和失败请求，检查 `x-token`、状态码、响应字段和数据库行数。

## Session 7-10：分页、筛选、序号、统计

**依赖：** Session 5-6。

- [x] 新增分页请求、`PageResult`、分页 VO 和统计 VO；分页参数设置默认值与上限，拒绝负数和过大 pageSize。
- [x] DAO 增加 count 和分页查询；筛选支持姓名、手机号、邮箱、岗位、渠道、初/复试对接人、状态和创建日期范围；SQL 使用参数绑定，不拼接用户输入。
- [x] Service 计算跨页 `serialNo`，按稳定排序返回结果。
- [x] 增加统计 SQL，返回待面试、待复试、待入职、未通过数量；不在内存中加载全表统计。
- [x] 测试第一页、末页、空结果、跨页序号、非法日期范围、多条件组合和统计准确性。
- [x] 分页和统计接口已在 Apifox 调用并保存响应示例。

## Session 11：状态枚举和状态历史

**依赖：** Session 5；必须先完成基础招聘记录。

- [x] 创建 `RecruitmentStatus`、`StatusTransitionAction`、`RecruitmentStatusHistoryPo/VO` 和 `V2__create_recruitment_status_history.sql`。
- [x] 创建历史 DAO XML，操作人字段使用当前登录 `user.id`，不得接受客户端 operator。
- [x] 创建 `RecruitmentStatusService`，以不可变规则表校验合法流转；状态更新和历史插入使用同一事务。
- [x] 从基础更新 Service 中移除直接修改状态的入口；状态动作通过状态服务变更状态。
- [x] 测试合法流转、越级流转、终态重复操作、历史记录的来源状态、目标状态、动作、操作人字段、备注、创建时间和历史写入失败；测试状态历史查询的不存在记录返回 404、Mapper 按 ID 升序结果的顺序和字段映射；测试删除无状态历史记录成功、有历史记录返回 409、空 UUID 返回 400、记录不存在返回 404 和删除未生效返回 500。
- [x] 已在 Apifox 保存状态动作和状态历史请求配置，并完成真实 HTTP 验证；有状态历史的删除请求返回 `409`，随后状态历史查询返回 `200`。

## Session 15-18：简历文件

**依赖：** Session 5、Session 11；文件路径必须限制在配置目录内。

- [x] Session 15：增加文件存储配置、10MB 大小上限和 PDF/DOC/DOCX 扩展名白名单；`FileUtils` 使用 UUID 加安全扩展名，并校验大小、配置和路径穿越。`FileStorageConfigurationTest` 与 `FileUtilsTest` 已验证。
- [x] Session 16：创建 `V3__create_candidate_resume.sql`、简历 PO/DAO/VO；表以 `record_uuid` 关联招聘记录。已在 study11 执行 V3 并完成结构测试；字段使用现有 `user.id` 与 `recruitment_info.record_uuid`。
- [x] Session 17：实现文件写入与元数据存储 Service；文件写入成功后再写元数据，数据库失败时删除已写入文件，文件失败时不写数据库。补充上传异常映射、原始文件名/MIME 校验、招聘删除简历预检、删除事务隔离与 `FOR UPDATE` 并发保护；未新增 HTTP 接口。
- [ ] Session 18：实现上传、列表、详情和下载 Controller；测试记录不存在、下载不存在和重复附件策略，并在 Apifox 验证每个接口，响应不得暴露本地绝对路径。

## Session 19-21：复试申请和确认

**依赖：** Session 11、Session 15。

- [ ] 创建招聘类型枚举、`V4__create_retest_tables.sql`、复试 PO/DAO/DTO/VO。
- [ ] 内部招聘不要求外派字段；外派招聘必须填写公司、对接人和复试时间。
- [ ] 申请只允许 `PENDING_INITIAL`，确认只允许 `RETEST_REVIEW`；状态变更写历史。
- [ ] 测试重复申请、越级确认、字段缺失、已淘汰记录和事务回滚。
- [ ] 在 Apifox 验证申请、确认、查询和非法状态请求。

## Session 22-23：录用通知

**依赖：** Session 11、Session 19-21。

- [ ] 创建 `V5__create_offer_notice.sql`、通知 PO/DAO/DTO/VO/Service/Controller。
- [ ] 第一阶段只保存草稿、收件邮箱、发送状态和时间；不接入真实 SMTP，不伪造“邮件已送达”。
- [ ] 发送只允许复试完成的记录，成功后流转到 `PENDING_ONBOARDING` 并写历史。
- [ ] 测试无邮箱、未完成复试、重复发送和状态历史。
- [ ] 在 Apifox 验证草稿、发送、查询和失败场景。

## Session 24-25：淘汰、放弃入职、人才库

**依赖：** Session 11。

- [ ] 创建原因、阶段、人才库分类枚举，`V6__create_recruitment_rejection.sql` 和对应分层类。
- [ ] 淘汰和放弃入职使用独立动作；分别流转到 `REJECTED`、`DECLINED`，并写状态历史。
- [ ] 终态禁止重复动作；人才库分类和原因按 DTO 校验。
- [ ] 测试任意允许阶段、分类必填、终态重复、固定放弃原因和事务回滚。
- [ ] 在 Apifox 验证淘汰、放弃和人才库查询。

## Session 26-27：入职与 study1-1 用户体系关联

**依赖：** Session 2-4、Session 11、Session 22-23。

- [ ] 创建 `V7__create_onboarding_record.sql` 和入职 PO/DAO/DTO/VO。
- [ ] 入职只允许 `PENDING_ONBOARDING`；在同一事务中向现有 `user` 表创建账号、使用现有 BCrypt 编码器保存密码、创建入职关联、写历史并更新招聘状态。
- [ ] 账号名按手机号生成，唯一冲突返回 409；初始密码只在本次响应返回，不写入招聘表或入职表。
- [ ] 不创建与 `user` 职责重复的 employee_account 表；`onboarding_record.user_id` 只引用 `user.id`。
- [ ] 测试账号冲突、重复办理、密码不明文和事务回滚。
- [ ] 在 Apifox 验证入职成功、账号查询、重复入职和冲突场景。

## Session 28：数据库集成测试

**依赖：** Session 1-27 的迁移脚本。

- [ ] 添加不影响默认测试的 MySQL 集成测试配置；优先使用已有测试基础设施，只有确实需要时才引入 Testcontainers。
- [ ] 验证所有表、主键、索引、外键、状态默认值和 `user` 表列集合未改变。
- [ ] 验证新增招聘记录时 `id` 自动生成且不是主键。
- [ ] 运行集成测试并在 Apifox 验收库复核真实 SQL。

## Session 29：参数校验与统一 API 文档边界

**依赖：** Session 5-27。

- [ ] 给所有新增 DTO 添加 `@NotBlank`、`@Email`、`@Size`、`@Positive`、日期范围校验等注解。
- [ ] 复用现有 `ErrorResponse`，不使用全局 ResponseBodyAdvice 改写旧登录和用户响应；补充招聘错误码和 409/422 映射。
- [ ] 补充接口文档，记录请求方式、路径、参数、登录要求、成功/失败响应和 Apifox 示例。
- [ ] 在 Apifox 对所有参数校验接口执行至少一次错误请求。

## Session 30：端到端流程

**依赖：** Session 11、19-27。

- [ ] 使用 MockMvc 或真实测试数据库跑通“招聘 -> 申请复试 -> 确认 -> 通知 -> 入职创建 user”流程。
- [ ] 跑通“招聘 -> 淘汰 -> 人才库”流程。
- [ ] 断言每次状态变化、状态历史、操作人 `user.id` 和事务结果。
- [ ] 在 Apifox 按相同顺序重放两条流程并记录环境变量。

## Session 31：最终验收

**依赖：** Session 28-30。

- [ ] 执行 `./mvnw.cmd clean test`。
- [ ] 执行 `./mvnw.cmd package`，确认无编译、依赖或资源错误。
- [ ] 启动 `study1-1`，验证注册、登录、Token、用户查询、修改密码和招聘全流程。
- [ ] 检查所有 Spring Bean、Mapper XML namespace、SQL 字段、事务和日志敏感信息。
- [ ] 在 Apifox 完成全量接口回归，并保存环境、接口和示例响应。
- [ ] 更新项目迁移说明和 Session 进度，不修改 study2。
