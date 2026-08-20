# study1-1 项目说明

## 项目信息
- 项目类型：Java Spring Boot 学习项目
- GitHub仓库：https://github.com/niu-wu/study1.git

## Git分支策略
- **main分支**：稳定分支，不直接修改
- **dev分支**：开发分支，所有开发工作在此进行
- 默认工作分支：dev

## 开发规范
- 所有代码改动在dev分支进行
- 测试通过后再合并到main
- 提交信息使用中文描述

## 验证命令
```bash
# 运行测试
./mvnw test

# 启动项目
./mvnw spring-boot:run
```
