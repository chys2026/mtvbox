# TVBox APK 打包详细教程

本文档详细说明如何从源代码构建 TVBox 应用的 APK 文件，包括本地构建和使用 GitHub Actions 自动化构建两种方式。

## 目录

1. [开发环境准备](#1-开发环境准备)
2. [获取源代码](#2-获取源代码)
3. [项目配置修改](#3-项目配置修改)
4. [本地构建 APK](#4-本地构建-apk)
5. [APK 签名](#5-apk-签名)
6. [GitHub Actions 自动化构建](#6-github-actions-自动化构建)
7. [常见问题排查](#7-常见问题排查)

---

## 1. 开发环境准备

### 1.1 安装 JDK

项目需要 JDK 11。

**Windows:**
```
# 下载 Oracle JDK 11 或 OpenJDK 11 并安装
# 设置环境变量 JAVA_HOME
```

**macOS:**
```bash
brew install openjdk@11
```

**Linux (Ubuntu/Debian):**
```bash
apt-get install -y openjdk-11-jdk
```

验证安装:
```bash
java -version
# 输出: openjdk version "11.0.x"
```

### 1.2 安装 Android SDK

推荐通过 Android Studio 安装，或使用命令行工具 `sdkmanager`。

**通过 Android Studio 安装（推荐）:**
- 下载 [Android Studio](https://developer.android.com/studio)
- 安装时勾选 Android SDK
- SDK Platforms: 勾选 **Android 13.0 (API 33)**
- SDK Tools: 勾选 **Android SDK Build-Tools**, **NDK**, **CMake**

**通过命令行安装:**
```bash
# 下载 commandlinetools
# 解压到 ~/android-sdk/

# 安装必要组件
cd ~/android-sdk/cmdline-tools/latest/bin
./sdkmanager "platforms;android-33" "build-tools;33.0.0" "ndk;21.4.7075529"
```

### 1.3 设置环境变量

创建或编辑 `~/.bashrc` / `~/.zshrc`:

```bash
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
```

使环境变量生效:
```bash
source ~/.bashrc
```

---

## 2. 获取源代码

```bash
# 克隆项目
git clone --depth 1 https://github.com/你的仓库/TVBoxMobile.git

# 进入项目目录
cd TVBoxMobile
```

### 2.1 项目结构概览

```
TVBoxMobile/
  app/                    # 主应用模块
  player/                 # 播放器库模块
  quickjs/                # QuickJS 引擎模块
  crash/                  # 崩溃处理模块
  TabLayout/              # Tab 组件模块
  ViewPager1Delegate/     # ViewPager 委托模块
  build.gradle            # 顶层构建配置
  settings.gradle         # 模块配置
  gradle.properties       # Gradle 属性
  .github/workflows/      # GitHub Actions 配置
```

---

## 3. 项目配置修改

### 3.1 修改应用 ID（包名）

编辑 `app/build.gradle`:

```groovy
defaultConfig {
    applicationId 'com.yourdomain.tvbox'  // 改为你自己的包名
}
```

### 3.2 修改版本号

```groovy
defaultConfig {
    versionCode 1           // 版本号（整数，每次发布递增）
    versionName '1.0.0'     // 版本名称（用户可见）
}
```

### 3.3 配置签名

#### 方式一：生成新签名

```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-alias -storepass 你的密码 -keypass 你的密码 \
  -dname "CN=Your Name, OU=Your Org, O=Your Company, L=City, S=State, C=CN"
```

复制 `my-release-key.jks` 到项目根目录。

更新 `app/build.gradle`:

```groovy
signingConfigs {
    config {
        keyAlias 'my-alias'
        keyPassword '你的密码'
        storeFile file('../my-release-key.jks')
        storePassword '你的密码'
    }
}
```

#### 方式二：使用环境变量（CI 安全推荐）

```groovy
signingConfigs {
    config {
        keyAlias System.getenv('KEY_ALIAS') ?: 'default'
        keyPassword System.getenv('KEY_PASSWORD') ?: 'default'
        storeFile file(System.getenv('KEYSTORE_PATH') ?: '../TVBoxOSC.jks')
        storePassword System.getenv('KEYSTORE_PASSWORD') ?: 'default'
    }
}
```

### 3.4 修改 ABI 过滤（可选）

默认仅构建 arm64-v8a 架构，如需构建 armeabi-v7a:

```groovy
ndk {
    abiFilters "arm64-v8a", "armeabi-v7a"
}
```

### 3.5 修改 API 服务器地址

编辑 `app/src/main/java/com/github/tvbox/osc/api/ApiConfig.java`:

```java
// 修改为你的 API 服务器地址
public static final String API_URL = "https://your-server.com/api";
```

---

## 4. 本地构建 APK

### 4.1 使用 Gradle 命令行

**Windows:**
```bat
gradlew.bat assembleRelease
```

**macOS / Linux:**
```bash
chmod +x gradlew
./gradlew assembleRelease
```

### 4.2 使用 Android Studio

1. 用 Android Studio 打开项目目录
2. 等待 Gradle 同步完成
3. 菜单栏: Build -> Build Bundle(s) / APK(s) -> Build APK(s)
4. 构建完成后，点击弹窗中的 "locate" 查看 APK

### 4.3 构建输出位置

```
app/build/outputs/apk/release/MBox_v1.1.1_release_20260101.apk
```

### 4.4 构建变体

```bash
# Release 版本（不含调试信息）
./gradlew assembleRelease

# Debug 版本（含调试信息，用于测试）
./gradlew assembleDebug

# 清理并重新构建
./gradlew clean assembleRelease
```

### 4.5 Gradle 构建优化

编辑 `gradle.properties`:

```properties
# 开启并行构建
org.gradle.parallel=true

# 开启构建缓存
org.gradle.caching=true

# 设置 JVM 内存
org.gradle.jvmargs=-Xmx4608m -XX:MaxPermSize=512m

# 开启 Daemon
org.gradle.daemon=true
```

---

## 5. APK 签名

如果 build.gradle 中已配置签名，`assembleRelease` 会自动签名。

### 5.1 验证签名

```bash
# 查看 APK 签名信息
keytool -printcert -jarfile app/build/outputs/apk/release/*.apk
```

### 5.2 手动签名（如果构建时未签名）

```bash
# 对齐 APK
zipalign -v -p 4 unsigned.apk aligned.apk

# 签名
apksigner sign --ks my-release-key.jks --out signed.apk aligned.apk
```

---

## 6. GitHub Actions 自动化构建

### 6.1 工作流说明

项目包含两个 GitHub Actions 工作流:

| 文件 | 用途 | 触发方式 |
|------|------|---------|
| `test.yml` | 测试构建，支持多 ABI | 手动触发 (workflow_dispatch) |
| `release.yml` | 正式发布，自动打 Release | Git Tag 推送 或 手动触发 |

### 6.2 配置 Secrets

在 GitHub 仓库中配置签名密钥:

1. 进入仓库 Settings -> Secrets and variables -> Actions
2. 添加以下 Secrets:

| Secret 名称 | 说明 |
|------------|------|
| `KEYSTORE_BASE64` | JKS 签名文件的 Base64 编码 |

**生成 KEYSTORE_BASE64:**
```bash
base64 your-keystore.jks | tr -d '\n'
```

将输出的字符串粘贴到 GitHub Secret 中。

### 6.3 通过手动触发构建

1. 进入仓库 Actions 页面
2. 选择 "Test Build" 或 "Release Build"
3. 点击 "Run workflow"
4. 选择参数（如果需要）
5. 点击 "Run workflow" 按钮

### 6.4 通过 Git Tag 触发发布

```bash
# 标记版本
git tag v1.1.2

# 推送标签（触发 release.yml）
git push origin v1.1.2
```

推送后，GitHub Actions 自动:
1. 构建 arm64-v8a 和 armeabi-v7a 两个版本的 APK
2. 收集所有 APK
3. 生成更新日志
4. 创建 GitHub Release 并上传 APK

### 6.5 下载构建产物

1. 进入 Actions 页面
2. 点击某次构建运行
3. 在 Artifacts 区域下载 `TVBoxOSC-arm64-v8a` 或 `TVBoxOSC-armeabi-v7a`

---

## 7. 常见问题排查

### 7.1 Gradle 下载依赖失败

项目使用阿里云 Maven 镜像，如果网络问题可修改代理:

编辑 `build.gradle`:

```groovy
allprojects {
    repositories {
        maven { url "https://maven.aliyun.com/repository/public" }
        google()
        mavenCentral()
    }
}
```

### 7.2 NDK not found

如果报错 "NDK not found":

**方式一：安装 NDK**
```bash
sdkmanager "ndk;21.4.7075529"
```

**方式二：使用预置 .so**
项目 `player/` 模块的 `jniLibs/` 目录已经包含编译好的 .so 文件，无需额外配置 NDK。

### 7.3 编译内存不足

修改 `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4608m -XX:MaxPermSize=512m
```

或在 `app/build.gradle` 中:

```groovy
dexOptions {
    javaMaxHeapSize "4g"
}
```

### 7.4 签名文件路径问题

Windows 使用反斜杠 `\`，Linux/Mac/CI 使用正斜杠 `/`:

```groovy
// 跨平台兼容写法
storeFile file('../TVBoxOSC.jks')
```

### 7.5 多 DEX 问题

项目已配置 multiDex，如果仍然报错:

```groovy
defaultConfig {
    multiDexEnabled true
}

dexOptions {
    javaMaxHeapSize "4g"
    additionalParameters += '--multi-dex'
    additionalParameters += '--set-max-idx-number=48000'
    additionalParameters += '--minimal-main-dex'
}
```

### 7.6 APK 安装失败

常见原因:
- **签名不一致**: 卸载旧版本后重新安装
- **最低 SDK 版本不匹配**: 项目最低 SDK 为 24 (Android 7.0)
- **架构不匹配**: arm64-v8a APK 无法在 32 位设备上安装

### 7.7 GitHub Actions 构建失败

- 检查是否有签名文件: 确保 `TVBoxOSC.jks` 存在于项目根目录
- 检查 Gradle 版本: 使用 Gradle 7.2.2 (wrapper 已包含)
- 网络问题: 项目使用阿里云镜像，通常不会因网络问题失败

---

## 附录: 快速构建检查清单

- [ ] JDK 11 已安装并正确配置
- [ ] Android SDK API 33 已安装
- [ ] 签名密钥已生成并放置在项目根目录
- [ ] `app/build.gradle` 中签名配置正确
- [ ] 应用 ID (包名) 已修改
- [ ] 版本号已更新
- [ ] `./gradlew assembleRelease` 成功执行
- [ ] APK 文件存在于 `app/build/outputs/apk/release/`
- [ ] GitHub Secrets 已配置（如使用 CI）
- [ ] GitHub Actions 构建成功并下载 APK 验证
