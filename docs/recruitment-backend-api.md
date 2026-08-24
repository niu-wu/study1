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

后续简历、复试申请、录用通知、淘汰/人才库和入职接口尚未实现，不在本记录中标记为已验收。
