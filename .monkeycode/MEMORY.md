# 用户指令记忆

本文件记录了用户的指令、偏好和教导，用于在未来的交互中提供参考。

## 格式

### 用户指令条目
用户指令条目应遵循以下格式：

[用户指令摘要]
- Date: [YYYY-MM-DD]
- Context: [提及的场景或时间]
- Instructions:
  - [用户教导或指示的内容，逐行描述]

### 项目知识条目
Agent 在任务执行过程中发现的条目应遵循以下格式：

[项目知识摘要]
- Date: [YYYY-MM-DD]
- Context: Agent 在执行 [具体任务描述] 时发现
- Category: [运维部署|构建方法|测试方法|排错调试|工作流协作|环境配置]
- Instructions:
  - [具体的知识点，逐行描述]

## 去重策略
- 添加新条目前，检查是否存在相似或相同的指令
- 若发现重复，跳过新条目或与已有条目合并
- 合并时，更新上下文或日期信息
- 这有助于避免冗余条目，保持记忆文件整洁

## 条目

### TVBox APK 构建方法
- Date: 2026-06-23
- Context: Agent 在分析项目结构和构建配置时发现
- Category: 构建方法
- Instructions:
  - 使用 Gradle 7.2.2 + Android Gradle Plugin 7.2.2 构建，JDK 11
  - 构建命令: `./gradlew assembleRelease`
  - 构建产物: `app/build/outputs/apk/release/MBox_v*_release_YYYYMMDD.apk`
  - 默认构建 arm64-v8a ABI，改为 armeabi-v7a 需修改 `app/build.gradle` 中 ndk.abiFilters
  - 签名配置在 `app/build.gradle` 中，使用项目根目录下的 `TVBoxOSC.jks`
  - 依赖阿里云 Maven 镜像加速

### GitHub Actions 自动化构建
- Date: 2026-06-23
- Context: Agent 在优化 CI/CD 流程时发现
- Category: 构建方法
- Instructions:
  - `test.yml`: 手动触发构建，支持选择 arm64-v8a / armeabi-v7a / all
  - `release.yml`: Git Tag (v*) 或手动触发，自动构建双 ABI 并创建 GitHub Release
  - CI 中签名: 需要将 JKS 文件 Base64 编码后存入 GitHub Secret `KEYSTORE_BASE64`
  - CI 构建时通过 sed 修改 ABI 过滤参数来切换架构
