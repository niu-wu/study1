本轮完成情况
- 在现有 user、登录、注册、BCrypt 密码和 x-token 鉴权体系上，补齐招聘后续流程。
- 已实现简历接口、复试、录用通知、淘汰/人才库、入职账号关联、参数校验和跨模块自动化回归。
- 当前执行 .\mvnw.cmd test：187 项通过，失败 0、错误 0、跳过 4 项可选数据库集成测试。
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