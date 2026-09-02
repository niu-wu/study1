# study1-1 Git 发布提交批次

本次只在本地 `dev` 分支创建提交，远程推送由项目所有者执行。

## 提交顺序

1. `3d3cf0a`：开发进度、接口记录和计划文档。
2. `bffb363`：用户角色、当前用户接口、后端权限校验、V8/V9 迁移及相关测试。
3. `e672261`：招聘岗位、公司配额、V10/V11 迁移、招聘关联、Mapper、服务和测试。
4. 本提交：补充本发布批次说明。

## 不纳入仓库

`target/`、`.idea/`、本地上传简历、运行日志、Apifox 临时截图、两个根目录数据库备份 SQL，以及任何未明确需要的本地生成文件继续由 `.gitignore` 排除。

## 发布前检查

- 运行 `./mvnw.cmd clean test`。
- 运行 `./mvnw.cmd package`。
- 检查 `git status --short --branch` 和 `git log --oneline -4`。
- 不在本地代理流程中执行 `git push`。
