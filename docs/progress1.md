  **文件改动明细：**
- .gitignore：忽略运行时简历目录 /data/recruitment-resumes/。
- pom.xml：增加 Flyway Core/MySQL 依赖、版本属性及 Maven Flyway 插件配置。
- src/main/resources/application.yml：增加 Flyway、Multipart 文件大小限制和 file.storage 配置。
- src/main/resources/db/migration/V1__create_recruitment_info.sql：创建独立 recruitment_info 表；record_uuid 为主键，自动生成的 id 为唯一非主键编号。
- src/main/resources/db/migration/V2__create_recruitment_status_history.sql：创建状态历史表，关联招聘记录与现有 user.id。
- src/main/resources/db/migration/V4__create_retest_tables.sql：创建复试申请与审核表，关联现有 user.id 和 recruitment_info.record_uuid。
- src/main/resources/db/migration/V5__create_offer_notice.sql：创建录用通知草稿/模拟发送表，复用现有 user.id，不接入 SMTP。
- src/main/resources/db/migration/V6__create_recruitment_rejection.sql：创建淘汰/放弃入职及人才库归档表，关联招聘记录、简历附件和现有 user.id。
- src/main/resources/db/migration/V7__create_onboarding_record.sql：创建入职关联表，关联招聘记录与现有 user.id，不保存初始密码。
- src/main/resources/mapper/RecruitmentInfoMapper.xml、RecruitmentStatusHistoryMapper.xml：实现招聘 CRUD、分页筛选、统计、状态更新及状态历史 SQL。
- src/main/java/com/example/study11/controller/RecruitmentInfoController.java：增加招聘 CRUD、分页、统计、状态动作和状态历史接口。
- src/main/java/com/example/study11/service/RecruitmentInfoService.java、RecruitmentStatusService.java：定义招聘管理与状态流转业务接口。
- src/main/java/com/example/study11/service/impl/RecruitmentInfoServiceImpl.java：实现 UUID 创建、初始状态、筛选分页序号、统计、更新保护和带历史检查的删除。
- src/main/java/com/example/study11/service/impl/RecruitmentStatusServiceImpl.java：实现固定状态机、认证用户操作人、事务内历史写入及历史查询。
- src/main/java/com/example/study11/service/impl/RetestServiceImpl.java：实现复试申请、复试确认、外派字段校验和状态历史。
- src/main/java/com/example/study11/service/impl/OfferNoticeServiceImpl.java：实现录用通知草稿、模拟发送、邮箱/状态校验和待入职状态流转。
- src/main/java/com/example/study11/service/impl/RejectionServiceImpl.java：实现任意非终态淘汰、待入职放弃、人才库筛选和附件归属校验。
- src/main/java/com/example/study11/service/impl/OnboardingServiceImpl.java：复用现有 user 服务创建账号，办理入职并通过状态服务写入 ONBOARDED 历史。
- src/main/java/com/example/study11/dao/RecruitmentInfoDao.java、RecruitmentStatusHistoryDao.java：新增对应数据访问接口。
- src/main/java/com/example/study11/dao/RetestApplicationDao.java、RetestReviewDao.java、OfferNoticeDao.java：新增复试与录用通知数据访问接口。
- src/main/java/com/example/study11/dao/RecruitmentRejectionDao.java：新增淘汰与人才库数据访问接口。
- src/main/java/com/example/study11/dao/OnboardingRecordDao.java：新增入职记录数据访问接口。
- src/main/java/com/example/study11/entity/dto/RecruitmentInfoCreateDTO.java、RecruitmentInfoUpdateDTO.java、RecruitmentInfoPageRequest.java、RecruitmentStatusTransitionDTO.java：新增请求校验模型。
- src/main/java/com/example/study11/entity/enums/RecruitmentStatus.java、StatusTransitionAction.java：定义招聘状态及允许动作。
- src/main/java/com/example/study11/entity/enums/RecruitmentType.java、NoticeStatus.java：定义招聘类型和录用通知状态。
- src/main/java/com/example/study11/entity/enums/RejectionStage.java、RejectionReason.java、TalentCategory.java：定义淘汰阶段、原因和人才库分类。
- src/main/java/com/example/study11/entity/po/RecruitmentInfoPo.java、RecruitmentInfoStatisticsPo.java、RecruitmentStatusHistoryPo.java：新增持久化模型。
- src/main/java/com/example/study11/entity/vo/RecruitmentInfoVO.java、RecruitmentInfoStatisticsVO.java、RecruitmentStatusHistoryVO.java、common/model/PageResult.java：新增接口响应和分页模型。
- src/main/java/com/example/study11/entity/vo/RetestDetailsVO.java、OfferNoticeVO.java：新增复试和录用通知响应模型。
- src/main/java/com/example/study11/entity/vo/RecruitmentRejectionVO.java：新增淘汰与人才库响应模型。
- src/main/java/com/example/study11/entity/vo/OnboardingRecordVO.java：新增入职记录响应模型，初始密码仅在办理成功响应中返回。
- src/main/java/com/example/study11/config/FileStorageProperties.java、utils/FileUtils.java：实现 PDF/DOC/DOCX 白名单、10MB 校验、UUID 文件名及路径穿越防护。
- src/test/java/com/example/study11/config/FileStorageConfigurationTest.java、FlywayConfigurationTest.java：覆盖配置契约。
- src/test/java/com/example/study11/controller/RecruitmentInfoControllerTest.java：覆盖分页、统计、状态动作和状态历史接口。
- src/test/java/com/example/study11/recruitment/schema/RecruitmentSchemaMigrationTest.java、RecruitmentStatusHistoryMigrationTest.java：覆盖迁移表结构与外键。
- src/test/java/com/example/study11/service/impl/RecruitmentInfoPaginationServiceTest.java、RecruitmentInfoServiceImplTest.java、RecruitmentStatusServiceImplTest.java：覆盖招聘业务与状态机行为。
- src/test/java/com/example/study11/service/impl/RetestServiceImplTest.java、OfferNoticeServiceImplTest.java、controller/RetestControllerTest.java、controller/OfferNoticeControllerTest.java：覆盖复试与录用通知成功、状态、重复和参数失败场景。
- src/test/java/com/example/study11/recruitment/schema/RetestSchemaMigrationTest.java、OfferNoticeSchemaMigrationTest.java：覆盖 V4/V5 字段、索引和外键契约。
- src/test/java/com/example/study11/service/impl/RejectionServiceImplTest.java、controller/RejectionControllerTest.java、recruitment/schema/RejectionSchemaMigrationTest.java：覆盖淘汰、放弃、人才库筛选、重复处理和 V6 结构契约。
- src/test/java/com/example/study11/service/impl/OnboardingServiceImplTest.java、src/test/java/com/example/study11/controller/OnboardingControllerTest.java：覆盖账号创建、一次性密码、冲突、重复办理、状态和鉴权来源。
- src/test/java/com/example/study11/recruitment/schema/OnboardingSchemaMigrationTest.java：覆盖 V7 字段、唯一约束、外键和不含密码列的契约。
- src/test/java/com/example/study11/recruitment/integration/Study11DatabaseIntegrationTest.java：受 `STUDY11_INTEGRATION_TESTS=true` 开关控制，直连 MySQL 验证表、主键、唯一索引、外键、状态默认值、user 列集合及招聘编号生成规则。
- src/test/java/com/example/study11/utils/FileUtilsTest.java：覆盖文件扩展名、大小、路径及非法配置。
- docs/recruitment-backend-api.md、docs/superpowers/plans/2026-08-23-study1-recruitment-backend.md、docs/superpowers/specs/2026-08-23-study2-to-study1-recruitment-design.md：记录接口、迁移设计和后续计划。
- study11_backup_before_recruitment_20260823_213144.sql：数据库备份文件，包含 user 表数据。
- study11_backup_before_v2_v3_20260824_141330.sql：V2/V3 前数据库备份文件，必须保留。
  **完成情况/注意事项：**
- 本节状态按用户于 2026-08-26 确认的 Apifox 实测结果更新；不在本次记录中保存 Token、密码或其他敏感值。
- Session 15-18 已完成文件存储配置、简历实体、文件存储服务和四个简历接口的后端实现；自动化测试及 Apifox 成功/失败验证均已完成。
- Session 19-23 的复试和录用通知后端代码、V4/V5 迁移、自动化测试、本地 HTTP 验证及 Apifox 成功/失败验证均已完成。
- Session 24-25 的淘汰、放弃入职和人才库后端代码、V6 迁移、自动化测试、本地 HTTP 验证及 Apifox 成功/失败验证均已完成。
- Session 26-27 入职后端已完成；V7 已执行到 study11，自动化测试和本地 HTTP 验证通过。办理入职响应只返回一次性初始密码，详情查询不返回密码；数据库中用户密码为 BCrypt 摘要，`onboarding_record` 无密码字段。重复办理和手机号冲突均返回 `409`。Apifox 已保存并验证 `OnboardingProcess`（`201`）、`OnboardingDetails`（`200`）和重复办理（`409`）。
- Session 18-25 的 Apifox 逐接口验收已完成，覆盖简历、复试、录用通知、淘汰和人才库接口的成功、鉴权失败、参数错误、重复操作及状态限制场景。
- Session 28 数据库集成测试已完成：默认 Maven 测试保持禁用，显式连接 study11 时 4 项测试全部通过；临时招聘行会在测试后删除。真实 SQL 复核及入职接口的 Apifox 验收已记录在 `docs/recruitment-backend-api.md`。
- Session 29 参数校验已完成：新增 DTO、路径参数和文件参数边界校验，统一校验异常返回 `400`；`Session29ValidationTest` 6 项通过。Apifox 已完成非法手机号、畸形 UUID、非法附件编号、空文件、空白 `recordUuid` 及缺少 `x-token` 等错误请求验证。
- Session 30 端到端自动化已完成：`Session30EndToEndTest` 2 项 MockMvc 流程通过，覆盖“招聘 -> 申请复试 -> 确认 -> 录用通知 -> 入职创建 user”和“招聘 -> 淘汰 -> 人才库”，并断言状态历史、操作人和事务结果。两条流程已在 Apifox 按相同顺序完成重放，并核对环境变量与状态历史。
- 最新构建复核已完成：`./mvnw.cmd clean test` 共 198 项测试，失败 0、错误 0、跳过 4；`./mvnw.cmd package` 为 `BUILD SUCCESS`；`git diff --check` 通过（仅有 CRLF 转换提示）。Session 31 的启动回归、注册/登录/用户/鉴权检查及 Apifox 全量接口回归均已完成。
- Spring Flyway 默认关闭，需要受控执行迁移。
- 两个 study11 备份 SQL 文件包含用户数据，必须保留但提交前应确认是否允许纳入版本库。

## Session 32-34：用户角色、岗位与公司配额后端基础（2026-09-01）

**依赖：** Session 31；前端继续暂停。

- [x] V9 `user.role` 已加入，角色枚举固定为 `USER`、`HR`、`ADMIN`，默认 `USER`；未修改现有测试账号角色，客户端 DTO 不接受角色覆盖。
- [x] 新增 `RoleAuthorizationService`，岗位/公司接口由后端读取 `user` 表并校验 HR/ADMIN；无效会话返回 `401`，普通用户返回 `403`。
- [x] 新增 `/users/me` 查询和更新接口，仅允许修改当前用户基础资料，角色、状态和逻辑删除字段由后端保留。
- [x] V10 已创建 `recruitment_company`、`recruitment_job`、`recruitment_job_company`、`recruitment_job_status_history`；岗位数字 `id` 为自增唯一非主键，岗位软删除与岗位状态分离，公司配额状态独立维护。
- [x] V11 已为 `recruitment_info` 增加可空 `job_uuid`、`job_company_allocation_uuid` 外键，保留历史 `position` 快照。
- [x] 新增岗位创建、分页、详情、编辑、状态流转、软删除和公司选项查询的后端分层代码；岗位职责使用 Jsoup 清洗 HTML。
- [x] 新增角色、岗位、公司服务测试、Controller 测试、V9-V11 迁移结构测试和真实 MySQL 集成契约测试。
- [x] 受控执行 Flyway 到 v11；全量 Maven 测试 198 项通过、失败 0、错误 0、跳过 4；数据库集成契约测试此前已验证 4 项通过。

**Apifox 验收记录（本轮，需使用 `x-token: {{token}}`）：**

- 环境：`http://localhost:8081`；管理员测试用户编号为 `23`（角色 `ADMIN`）；文档不保存 Token 或密码。
- 当前用户：`GET /users/me`、`PUT /users/me` 均返回 `200`；响应包含角色且不返回密码，客户端提交的角色、状态和逻辑删除字段未覆盖后端值。
- 公司选项：`GET /api/recruitment-companies/options` 返回 `200`，返回未删除且 `ACTIVE` 的测试公司选项。
- 岗位 CRUD：创建返回 `201`；分页、详情和编辑返回 `200`；岗位职责 HTML 已清洗，服务端维护 `status`、`isDeleted` 和版本字段。
- 岗位状态：`OPEN -> CLOSED`、`CLOSED -> OPEN` 均返回 `200`；重复目标状态返回 `409`，并写入 `scope=JOB` 的状态历史。
- 岗位删除：`DELETE /api/recruitment-jobs/{jobUuid}` 返回 `204`；删除后详情返回 `404`，分页结果不再展示该岗位，数据库保留软删除记录。
- 公司配额状态：岗位 `0be334cf-75b2-44a8-942e-43c9e9be52c7`、配额 `22222222-2222-4222-8222-222222222222` 的 `CLOSED -> OPEN` 返回 `200`，重复 `OPEN -> OPEN` 返回 `409`；数据库当前状态为 `OPEN`，审计记录使用 `scope=ALLOCATION`，操作人编号为 `23`，岗位整体状态未被改变。
- 边界抽查：缺少 `x-token` 返回 `401`；普通用户访问管理接口返回 `403`；合法状态路由下的畸形岗位 UUID 返回 `400`。
- 本轮仅验证后端接口和数据库状态，不执行前端页面、组件或 UI 验证。

## Session 35：岗位编辑与状态冲突接口验收（2026-09-02）

**依赖：** Session 32-34；前端继续暂停。

- [x] `PUT /api/recruitment-jobs/{jobUuid}` 使用 `x-token: {{token}}` 返回 `200`，岗位业务字段更新成功；客户端不可覆盖状态、软删除标识、版本和操作人。
- [x] `PATCH /api/recruitment-jobs/{jobUuid}/status`：`OPEN -> CLOSED` 返回 `200`；重复提交 `CLOSED -> CLOSED` 返回 `409`；`CLOSED -> COMPLETED` 返回 `422`；刷新登录令牌后验证 `COMPLETED -> OPEN` 同样返回 `422`，终态不可回退。
- [x] 本轮未保存真实 Token 或密码；仅验证后端接口和状态流转，前端页面及 UI 不在本次范围。
