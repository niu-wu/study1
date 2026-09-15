本轮完成情况
- 在现有 user、登录、注册、BCrypt 密码和 x-token 鉴权体系上，补齐招聘后续流程。
- 已实现简历接口、复试、录用通知、淘汰/人才库、入职账号关联、参数校验和跨模块自动化回归。
- 当前执行 .\mvnw.cmd test：198 项通过，失败 0、错误 0、跳过 4 项可选数据库集成测试。

## 2026-09-01 后端增量

- 完成 V9-V11：用户角色、独立岗位/公司/配额表、候选人与岗位关联。
- 新增岗位和公司选项后端接口、角色校验、软删除、状态与配额状态分离、岗位职责 HTML 清洗。
- 全量测试当前为 198 项通过，失败 0，错误 0，跳过 4；真实 MySQL 集成契约测试此前已验证 4 项通过。
- Apifox 验收已完成：环境为 `http://localhost:8081`，统一 Header 为 `x-token: {{token}}`，文档不保存 Token 或密码；覆盖当前用户、公司选项、岗位 CRUD、岗位状态和公司配额状态接口，包含成功、鉴权失败、普通用户 `403`、非法 UUID `400` 及重复状态 `409` 场景。前端任务继续标记为暂不执行。
- 公司配额验收证据：岗位 `0be334cf-75b2-44a8-942e-43c9e9be52c7`、配额 `22222222-2222-4222-8222-222222222222`；`CLOSED -> OPEN` 返回 `200`，重复 `OPEN -> OPEN` 返回 `409`；数据库 `recruitment_job_company.status=OPEN`，状态历史为 `ALLOCATION` 范围，操作人来自管理员用户编号 `23`，配额变更不改变岗位整体状态。

## Session 35：岗位编辑与状态冲突接口验收（2026-09-02）

- Apifox 使用 `baseUrl=http://localhost:8081` 和 `x-token: {{token}}` 完成 `PUT /api/recruitment-jobs/{jobUuid}` 验证，返回 `200`；岗位业务字段更新成功，状态、软删除标识、版本和操作人仍由后端维护。
- Apifox 完成岗位状态边界验证：`OPEN -> CLOSED` 返回 `200`；重复提交 `CLOSED -> CLOSED` 返回 `409`；`CLOSED -> COMPLETED` 返回 `422`；刷新登录令牌后验证 `COMPLETED -> OPEN` 返回 `422`，终态不可回退。
- 本轮不记录真实 Token 或密码，前端页面和 UI 验证继续暂不执行。
  文件改动明细
1. 招聘类型、复试与公共参数校验
- RecruitmentInfoCreateDTO.java、RecruitmentInfoUpdateDTO.java、RecruitmentInfoPo.java、RecruitmentInfoVO.java、RecruitmentInfoServiceImpl.java、RecruitmentInfoMapper.xml：新增并持久化 recruitmentType，默认内部招聘 INTERNAL。
- RecruitmentType.java：新增内部招聘、外派招聘枚举。
- RecruitmentInfoController.java、RecruitmentRequestValidator.java：补充招聘记录 UUID、正整数编号、手机号等请求参数校验。
- GlobalExceptionHandler.java：将 Spring 方法参数校验和缺失 multipart 文件字段统一响应为 400 Bad Request。
- RecruitmentInfoControllerTest.java、RecruitmentInfoServiceImplTest.java：补充招聘类型和请求校验测试。
- V4__create_retest_tables.sql：为 recruitment_info 增加 recruitment_type，新建 retest_application、retest_review 两张复试表，并关联现有 user 和招聘记录。
- RetestController.java、RetestService.java、RetestServiceImpl.java：实现复试申请、复试确认和复试详情查询接口。
- RetestApplicationDao.java、RetestReviewDao.java、RetestApplicationMapper.xml、RetestReviewMapper.xml：实现复试申请和审核数据访问。
- RetestApplicationRequest.java、RetestReviewRequest.java、RetestApplicationPo.java、RetestReviewPo.java、RetestApplicationVO.java、RetestReviewVO.java、RetestDetailsVO.java：新增复试请求、持久化和响应模型。
- RetestControllerTest.java、RetestServiceImplTest.java、RetestSchemaMigrationTest.java：覆盖复试状态流转、重复操作、外派字段校验和数据库结构。
2. 简历附件接口与文件安全
- ResumeController.java：新增简历上传、详情、列表和下载接口，所有接口受 Token 拦截保护。
- ResumeStorageService.java、ResumeStorageServiceImpl.java：补充简历元数据查询、列表查询和文件下载读取能力。
- FileUtils.java：加强文件名控制字符、路径穿越、扩展名和 MIME 类型处理；异常历史数据降级为安全下载响应。
- ResumeControllerTest.java、ResumeStorageServiceImplTest.java、FileUtilsTest.java：覆盖上传、下载、文件缺失、非法文件名、历史 MIME 降级和附件归属校验。
- InterceptorConfigTest.java：验证所有 /api/resumes/** 接口必须经过 Token 鉴权。
3. 录用通知流程
- V5__create_offer_notice.sql：新建录用通知草稿和模拟发送记录表，关联招聘记录及现有用户。
- OfferNoticeController.java、OfferNoticeService.java、OfferNoticeServiceImpl.java：实现录用通知草稿创建、模拟发送和详情查询。
- OfferNoticeDao.java、OfferNoticeMapper.xml：实现录用通知持久化。
- OfferNoticeDraftRequest.java、OfferNoticeSendRequest.java、OfferNoticePo.java、OfferNoticeVO.java、NoticeStatus.java：新增草稿、发送和状态模型。
- OfferNoticeControllerTest.java、OfferNoticeServiceImplTest.java、OfferNoticeSchemaMigrationTest.java：覆盖草稿、发送、状态推进、重复发送和迁移结构。
4. 淘汰、放弃入职与人才库
- V6__create_recruitment_rejection.sql：新建淘汰及人才库记录表，关联招聘记录、简历附件和操作用户。
- RejectionController.java、RejectionService.java、RejectionServiceImpl.java：实现淘汰候选人、放弃入职和人才库筛选接口。
- RecruitmentRejectionDao.java、RecruitmentRejectionMapper.xml：实现淘汰记录的数据访问。
- RejectRequest.java、DeclineRequest.java、RecruitmentRejectionPo.java、RecruitmentRejectionVO.java：新增淘汰和放弃入职模型。
- RejectionStage.java、RejectionReason.java、TalentCategory.java：新增淘汰阶段、原因和人才库分类枚举。
- RejectionControllerTest.java、RejectionServiceImplTest.java、RejectionSchemaMigrationTest.java：覆盖状态限制、重复处理、简历归属和人才库筛选。
5. 入职办理与现有用户体系关联
- V7__create_onboarding_record.sql：新建入职记录表，通过 user_id 关联 study1-1 现有 user 表，不保存密码。
- OnboardingController.java、OnboardingService.java、OnboardingServiceImpl.java：实现办理入职和查询入职记录接口。
- OnboardingRecordDao.java、OnboardingRecordMapper.xml：实现入职记录持久化。
- OnboardingRequest.java、OnboardingRecordPo.java、OnboardingRecordVO.java：新增入职请求、记录和响应模型。
- OnboardingControllerTest.java、OnboardingServiceImplTest.java、OnboardingSchemaMigrationTest.java：验证复用现有 UserService 创建账号、一次性初始密码仅在办理响应返回、手机号冲突和重复办理处理。
6. 跨模块回归测试
- Session29ValidationTest.java：覆盖招聘、简历、复试接口的 UUID、手机号、附件编号、空文件等非法参数响应。
- Session30EndToEndTest.java：覆盖“招聘 -> 复试 -> 录用通知 -> 入职创建用户”和“招聘 -> 淘汰 -> 人才库”两条完整流程。
- Study11DatabaseIntegrationTest.java：提供受环境变量控制的真实 MySQL 表结构、外键、唯一索引、招聘编号和 user 表契约验证。
7. 文档
- docs/progress1.md：更新 Session 15-30 的后端实现、自动化测试、Apifox 验收边界和待完成事项。
- docs/recruitment-backend-api.md：补充简历、复试、录用通知、淘汰、人才库和入职接口说明、请求示例及验证记录。

## 2026-09-15 员工档案三个小模块

- 完成 V14：员工主档档案字段、月度薪资快照、稼动事件日志、系统账号表（无密码列）。本机 `study11` Flyway 版本 14。
- 完成档案列表、全局统计、有限 PATCH、个人信息只读详情、一寸照、月度薪资、稼动状态机、系统账号接口。仅 HR/ADMIN。
- 薪资天数是 HR 手工可空快照，不接考勤、不参与应发/实发。应发/实发只由服务端计算。
- 稼动是事件日志：`ENTER`/`RETURN` 同一事务回写主档地点和客户；非法转换不落库。
- `./mvnw.cmd test`：313 项通过，失败 0，错误 0。入职登记、人员分配、兼职回归保持通过。
- Apifox 档案接口尚未补；`baseUrl` 使用当前启动端口（默认 `8080`）。
- 接口说明见 `docs/recruitment-backend-api.md`，交接摘要见 `docs/项目交接.md`。
