# study1-1 招聘后端接口记录

## 运行与鉴权

- 项目：`study1-1`
- 应用默认端口：`8080`（`application.yml` 配置）。
- 本次 Apifox 验证端口：`8081`，为显式本地启动覆盖，未改变默认端口。
- 本次验证启动命令：`java -jar target\study1-1-0.0.1-SNAPSHOT.jar --server.port=8081`。
- 数据库：`study11`
- 鉴权：登录后在请求头携带 `x-token: {{token}}`
- Apifox 项目：`study1-1 招聘管理后端`
- Apifox 测试环境：`测试环境`，`baseUrl=http://localhost:8081`；环境中维护 `token`、`recordUuid`、`recruitmentId`、`userId`

## 已验证接口

| 方法 | 路径 | 登录 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/recruitment-info` | 是 | 创建招聘记录；服务端生成 `recordUuid`、`id` 和初始状态 |
| GET | `/api/recruitment-info` | 是 | 条件列表查询 |
| GET | `/api/recruitment-info/{recordUuid}` | 是 | 按业务 UUID 查询 |
| GET | `/api/recruitment-info/by-id/{id}` | 是 | 按数据库自动编号查询 |
| PUT | `/api/recruitment-info/{recordUuid}` | 是 | 更新资料，不允许修改状态 |
| DELETE | `/api/recruitment-info/{recordUuid}` | 是 | 无状态历史时删除；已有状态历史时返回 `409` 并保留审计记录 |
| GET | `/api/recruitment-info/page` | 是 | 分页、筛选和跨页展示序号 |
| GET | `/api/recruitment-info/statistics` | 是 | 招聘状态统计 |
| POST | `/api/recruitment-info/{recordUuid}/status` | 是 | 执行合法状态动作，操作人来自已认证 Token |
| GET | `/api/recruitment-info/{recordUuid}/status-history` | 是 | 按 `id ASC` 查询状态历史；成功响应为未包装 JSON 数组 |
| POST | `/api/resumes/upload` | 是 | 上传 PDF/DOC/DOCX 简历，上传人从 `x-token` 解析 |
| GET | `/api/resumes/{id}` | 是 | 查询简历元数据，不返回存储文件名或服务器路径 |
| GET | `/api/resumes/download/{id}` | 是 | 下载简历，响应 MIME 和附件名受白名单控制 |
| GET | `/api/resumes/list/{recordUuid}` | 是 | 查询招聘记录下的全部简历元数据 |
| POST | `/api/retest/apply` | 是 | 申请复试，操作人从 Token 获取 |
| POST | `/api/retest/confirm` | 是 | 确认复试安排，外派招聘字段由服务端校验 |
| GET | `/api/retest/{recordUuid}` | 是 | 查询复试申请与安排 |
| POST | `/api/offers/draft` | 是 | 创建录用通知草稿，不发送邮件 |
| POST | `/api/offers/send` | 是 | 模拟发送录用通知并推进到待入职 |
| GET | `/api/offers/{recordUuid}` | 是 | 查询录用通知草稿或发送状态 |
| POST | `/api/rejections/reject` | 是 | 淘汰候选人并归档人才库分类 |
| POST | `/api/rejections/decline` | 是 | 放弃入职并归档人才库分类 |
| GET | `/api/rejections/talent-pool` | 是 | 按分类、原因、阶段筛选人才库 |
| POST | `/api/onboarding/process` | 是 | 办理入职并在现有 `user` 体系创建账号；初始密码只在成功响应出现 |
| GET | `/api/onboarding/{recordUuid}` | 是 | 查询入职记录和关联用户信息；不返回初始密码 |

## 状态动作

状态规则由服务端固定维护：

```text
PENDING_INITIAL -> RETEST_REVIEW -> PENDING_RETEST
PENDING_RETEST -> PENDING_ONBOARDING -> ONBOARDED
活动阶段 -> REJECTED / DECLINED
```

动作与状态转换如下：

```text
SUBMIT_RETEST_REVIEW: PENDING_INITIAL -> RETEST_REVIEW
CONFIRM_RETEST: RETEST_REVIEW -> PENDING_RETEST
COMPLETE_RETEST: PENDING_RETEST -> PENDING_ONBOARDING
COMPLETE_ONBOARDING: PENDING_ONBOARDING -> ONBOARDED
REJECT: any of PENDING_INITIAL, RETEST_REVIEW, PENDING_RETEST, PENDING_ONBOARDING -> REJECTED
DECLINE: same active statuses -> DECLINED
```

请求示例：

```json
{
  "action": "SUBMIT_RETEST_REVIEW",
  "remark": "初试通过"
}
```

客户端不能传入目标状态或 `operatorUserId`。状态更新与历史写入在同一事务中完成，历史操作人引用 `study11.user.id`。

## 状态历史

`GET /api/recruitment-info/{recordUuid}/status-history` 需要请求头 `x-token: {{token}}`。记录存在时返回 `200` 和未包装的 JSON 数组；每个历史对象包含 `id`、`recordUuid`、`fromStatus`、`toStatus`、`action`、`operatorUserId`、`remark`、`createdAt`，按 `id ASC` 排序。`recordUuid` 不存在时返回 `404`。

## 删除约束

`DELETE /api/recruitment-info/{recordUuid}` 需要请求头 `x-token: {{token}}`。没有状态历史的记录删除成功后返回 `204`；已有状态历史的记录返回 `409`，招聘记录和审计历史均保留；`recordUuid` 不存在时返回 `404`。

## Apifox 验证记录

- 分页接口：已保存为 `RecruitmentPage`，成功响应 `200`。
- 统计接口：已保存为 `Recruitment`，使用 `x-token: {{token}}`，成功响应 `200`，当前测试库统计包含 `pendingInitial` 数据。
- 状态动作接口：请求已保存；此前已验证合法动作返回 `200`，非法流转返回 `422`。
- 状态历史接口：已保存 `GET /api/recruitment-info/{recordUuid}/status-history`；在 `测试环境` 使用记录 `926adbce-cac7-4023-8de5-abbfc58289b8` 验证返回 `200`。响应模式已修正为 `array`（对象项），最终 `200` 响应校验通过；临时禁用 `x-token` 后返回 `401`，随后已恢复请求头并刷新环境令牌。
- 删除约束：在同一测试记录上使用 `DELETE /api/recruitment-info/926adbce-cac7-4023-8de5-abbfc58289b8`，返回 `409 Conflict`；随后使用当前 `x-token` 查询 `/status-history`，返回 `200` 和状态历史数组，确认招聘记录及审计历史均未被删除。
- 未登录招聘接口：返回 `401`。
- 非法日期范围、非法分页参数：返回 `400`。
- 入职接口：已在 Apifox 项目 `study1-1 招聘管理后端` 的 `测试环境` 保存 `OnboardingProcess` 和 `OnboardingDetails`。登录接口刷新后的 Token 已保存到环境变量 `token`，两个接口均使用 `x-token: {{token}}`。
- `POST /api/onboarding/process`：使用测试记录 `dccc0fe0-dedb-4a72-9c0c-62a9bc06b86a` 验证返回 `201`；响应中的一次性初始密码未写入文档或后续查询。
- `GET /api/onboarding/dccc0fe0-dedb-4a72-9c0c-62a9bc06b86a`：验证返回 `200`，状态为 `ONBOARDED`，`initialPassword` 为 `null`。
- 重复调用 `POST /api/onboarding/process`：验证返回 `409 Conflict`，错误消息为“该招聘记录已办理入职”。

## 简历接口

### 上传

`POST /api/resumes/upload` 使用 `multipart/form-data`，字段为 `recordUuid` 和 `file`。文件扩展名只允许 `pdf`、`doc`、`docx`，默认大小上限为 10 MB；数据库保存由 UUID 生成的内部文件名，但响应只返回原始文件名、大小、规范 MIME、上传人和时间。请求必须携带 `x-token: {{token}}`，上传人不能通过请求参数覆盖。

成功返回 `201`。记录不存在返回 `404`，缺少文件、扩展名不支持或文件大小不合法返回 `400`，未登录返回 `401`。

### 详情与列表

`GET /api/resumes/{id}` 返回单个附件元数据；`GET /api/resumes/list/{recordUuid}` 返回该招聘记录的附件数组。招聘记录或附件不存在返回 `404`。两个接口均要求登录。

### 下载

`GET /api/resumes/download/{id}` 返回文件流，并设置受控 `Content-Type` 和 `Content-Disposition: attachment`。未知或不一致的历史 MIME/文件名会降级为 `application/octet-stream` 与 `resume-{id}.bin`；元数据、物理文件或内部存储文件名损坏均返回 `404`。响应不暴露存储文件名、本地绝对路径或原始控制字符。

Session 18 自动化测试已覆盖上传、详情、列表、下载、缺少 multipart、记录不存在、附件不存在、物理文件不存在、路径/控制字符文件名、历史 MIME/扩展名降级、重复附件和 Token 用户来源。Session 18 的 Apifox 成功/失败请求待在运行中的 `8081` 服务上完成后补录。

## 复试接口

### 申请与确认

`POST /api/retest/apply` 请求体示例：

```json
{
  "recordUuid": "{{recordUuid}}",
  "applicantRemark": "初试通过"
}
```

仅允许 `PENDING_INITIAL` 记录申请，成功返回 `201` 并推进到 `RETEST_REVIEW`。重复申请返回 `409`，终态或其他状态返回 `422`。

`POST /api/retest/confirm` 请求体示例：

```json
{
  "recordUuid": "{{recordUuid}}",
  "retestCompany": "外派公司",
  "retestContactPerson": "李经理",
  "retestTime": "2026-08-30T10:00:00"
}
```

内部招聘可以省略外派字段，外派招聘三项必须同时填写。成功返回 `200` 并推进到 `PENDING_RETEST`；未申请、重复确认或非法状态返回 `409/422`。

### 查询

`GET /api/retest/{recordUuid}` 返回招聘状态、招聘类型、复试申请和复试安排。招聘记录不存在返回 `404`。

## 录用通知接口

### 创建草稿

`POST /api/offers/draft` 请求体：

```json
{
  "recordUuid": "{{recordUuid}}",
  "recipientEmail": "candidate@example.com",
  "noticeContent": "欢迎加入团队"
}
```

成功返回 `201`，状态为 `DRAFT`。草稿只保存数据，不调用 SMTP；同一招聘记录重复创建返回 `409`，终态记录返回 `422`。

### 模拟发送

`POST /api/offers/send` 请求体：

```json
{
  "recordUuid": "{{recordUuid}}"
}
```

服务端要求通知草稿存在、收件邮箱非空且招聘状态为 `PENDING_RETEST`。成功返回 `200`、通知状态 `SENT`，并通过 `COMPLETE_RETEST` 状态动作推进招聘记录到 `PENDING_ONBOARDING`，同时写入状态历史；不发送真实邮件。无邮箱返回 `400`，复试未完成返回 `422`，重复发送返回 `409`。

### 查询通知

`GET /api/offers/{recordUuid}` 返回收件邮箱、草稿内容、通知状态和发送时间，不返回 SMTP 凭证或其他认证信息。通知不存在返回 `404`。

## 淘汰与人才库接口

### 淘汰候选人

`POST /api/rejections/reject` 请求体示例：

```json
{
  "recordUuid": "{{recordUuid}}",
  "rejectionStage": "RETEST",
  "rejectionReason": "SKILL_MISMATCH",
  "talentCategory": "FUTURE_CONSIDER",
  "remark": "技能不匹配",
  "resumeAttachmentId": null
}
```

允许任意非终态招聘记录，成功返回 `201`，状态流转到 `REJECTED` 并写入状态历史。`talentCategory`、`rejectionStage` 和 `rejectionReason` 必填；关联简历附件必须属于当前招聘记录。终态记录返回 `422`，重复处理返回 `409`。

### 放弃入职

`POST /api/rejections/decline` 请求体示例：

```json
{
  "recordUuid": "{{recordUuid}}",
  "talentCategory": "NOT_SUITABLE",
  "remark": "候选人主动放弃"
}
```

仅允许 `PENDING_ONBOARDING`，服务端固定阶段为 `ONBOARDING`、原因为 `CANDIDATE_DECLINED`，成功返回 `200` 并流转到 `DECLINED`。重复放弃返回 `409`，其他状态返回 `422`。

### 人才库查询

`GET /api/rejections/talent-pool` 支持可选查询参数 `talentCategory`、`rejectionReason`、`rejectionStage`，返回按处理时间倒序的淘汰/放弃记录数组。参数使用未知枚举值时返回 `400`。

## 入职接口

### 办理入职

`POST /api/onboarding/process` 请求体示例：

```json
{
  "recordUuid": "{{recordUuid}}",
  "onboardingDate": "2026-09-01",
  "onboardingNote": "正式入职"
}
```

接口只允许 `PENDING_ONBOARDING` 招聘记录。服务端以候选人手机号作为 `user.username` 创建 study1-1 用户，使用现有 UserService 和 BCrypt 编码器写入 `user`，创建 `onboarding_record`，再通过状态服务流转到 `ONBOARDED`。办理人从已认证的 `x-token` 获取，客户端不能传入或覆盖操作人。

成功返回 `201`，响应中的 `initialPassword` 只出现本次办理结果，必须由调用方在安全链路中交付给候选人；密码不写入 `onboarding_record`、招聘表或日志。手机号已存在、记录已办理返回 `409`，记录状态不允许办理返回 `422`，手机号为空返回 `400`，未登录返回 `401`。

### 查询入职记录

`GET /api/onboarding/{recordUuid}` 返回入职记录、关联用户编号、用户名、入职日期和当前招聘状态，成功返回 `200`。该接口永远不返回初始密码；招聘记录或入职记录不存在返回 `404`。

Session 18-27 的自动化测试、V3-V7 迁移和本地 `8081` HTTP 验证已完成。Session 27 真实流程验证结果：办理入职 `201`，详情查询 `200` 且 `initialPassword=null`，重复办理 `409`，手机号冲突 `409`；数据库确认用户密码为 BCrypt 摘要、入职表无密码列、招聘状态为 `ONBOARDED`。Session 26-27 的入职接口已在 Apifox 完成上述逐接口验收；Session 18-25 的 Apifox 验收仍需后续补录，本地 HTTP 结果不冒充 Apifox 结果。

淘汰/人才库及入职后端实现已完成；前端展示和交互不在本次范围内。

## Session 29-31 验证边界

以下结果来自本地自动化测试或构建命令，不代表 Apifox 验收：

- Session 29：`Session29ValidationTest` 6 项通过，覆盖招聘手机号、招聘记录 UUID、简历编号、空文件和空 UUID 等非法参数，统一响应为 `400`。
- Session 30：`Session30EndToEndTest` 2 项通过，覆盖招聘到复试、录用通知、入职创建现有 `user` 的流程，以及招聘到淘汰、人才库查询的流程；测试同时断言状态历史、Token 用户对应的操作人和事务结果。
- 最新 Maven 复核：`./mvnw.cmd clean test` 为 187 项测试、失败 0、错误 0、跳过 4；`./mvnw.cmd package` 为 `BUILD SUCCESS`。`git diff --check` 通过，CRLF 提示不影响结果。

以下项目仍需在 Apifox 中实际执行并保存请求/响应，不能用上述自动化结果代替：

- Session 18-25 尚未补录的简历、复试、录用通知、淘汰和人才库逐接口成功/失败场景。
- Session 29 非法参数请求，包括缺少 `x-token` 的失败场景。
- Session 30 两条端到端流程的顺序重放及环境变量记录。
- Session 31 注册、登录、Token、用户接口、招聘全流程和全量接口回归。
