# 无极 Wuji

一款界面简洁、功能强大的 **跨平台资源聚合浏览器**。

基于 **Kotlin + Compose Multiplatform** 一套代码同时发布 **Android APK** 与 **Windows / macOS / Linux 桌面安装包**；聚合图片、音乐、书籍、漫画、视频五类资源的浏览、搜索、订阅源管理。

> 当前版本：`0.0.02`

---

## ✨ 功能特性

| 模块 | 说明 |
|------|------|
| 🖼️ **图片** | 内置 wallhaven 示例源；关键词搜索 / 瀑布流 / 高清详情大图 |
| 🎵 **音乐** | 多源聚合搜索 / 推荐 / 歌曲列表 / 底部迷你播放栏 |
| 📖 **书籍** | 书封卡片网格 / 详情章节列表 / 正文阅读器（上一章·下一章切换） |
| 🎨 **漫画** | 封面网格 / 详情与章节目录 / 纵向滚动图阅读 |
| 🎬 **视频** | 封面网格 / 元信息剧情简介 / 多播放源展示 |
| 📡 **订阅源管理** | URL 一键导入远程订阅源 / 启用禁用 / 删除 / 源市场 & 我的源入口 |
| ⚙️ **设置** | 浅色 / 深色 / 跟随系统 主题切换；账号入口；云同步入口；关于页 |

---

## 🏗️ 技术栈与架构

### 技术栈

| 方向 | 选型 |
|------|------|
| 语言 | Kotlin 2.1 |
| UI | Compose Multiplatform 1.7 + Material 3 |
| 架构 | MVVM + 分层（View / ScreenModel / Repository / Source） |
| 依赖注入 | Koin 4.0 |
| 导航 | Voyager 1.1（Screen + Tab + Koin 集成） |
| 网络 | Ktor 3 + OkHttp 引擎 |
| 图片加载 | Coil 3（Ktor3 网络层） |
| 订阅源 DOM 解析 | Ksoup（轻量 Jsoup 兼容 KMP 实现） |
| 持久化 KV | Multiplatform-Settings（Android DataStore / Desktop Preferences） |
| 序列化 | kotlinx-serialization-json |
| 日志 | Napier |
| 构建 | Gradle 8.7 + libs.versions.toml 版本目录 |

### 架构分层

```
┌─────────────────────────────────────────────────┐
│  View 层 (Activity / Screen / @Composable)      │  ← 纯 UI 展示与交互
├─────────────────────────────────────────────────┤
│  ScreenModel (Voyager StateScreenModel)         │  ← 页面状态机、业务编排
├─────────────────────────────────────────────────┤
│  Repository 层                                  │  ← 订阅源 / 设置 持久化
├─────────────────────────────────────────────────┤
│  SourceEngine + Extension (5 种资源类型)        │  ← 多源聚合抓取 & DOM 解析
├─────────────────────────────────────────────────┤
│  Core: Network / Storage / Platform / DOM       │  ← expect/actual 提供能力
└─────────────────────────────────────────────────┘
```

### 模块结构

```
wuji-compose/
├── build.gradle.kts                        # 根工程
├── settings.gradle.kts
├── gradle/libs.versions.toml               # 统一版本目录
└── composeApp/
    ├── build.gradle.kts                    # version = 0.0.02 / versionCode = 2
    ├── proguard-rules.pro
    └── src/
        ├── commonMain/
        │   └── com/wuji/app/
        │       ├── App.kt                  # 根 App: Koin + MainScreen(底部 5 Tab)
        │       ├── core/                   # DI / 网络 / 存储 / 平台
        │       ├── data/                   # SubscribeSourceRepository
        │       ├── source/                 # Extension/SourceEngine/5 种资源扩展
        │       └── ui/                     # theme / navigation / components / screen
        ├── androidMain/                    # MainActivity / SettingsProvider / Platform / Manifest
        └── desktopMain/                    # Main_desktopKt / SettingsProvider / Platform
```

---

## 🚀 本地构建与运行

### 环境要求

- JDK 21（Temurin 推荐）
- Android SDK Platform 35 + Build-Tools 35.0.0（Android 构建）
- 网络：可访问 Maven Central / Google Maven / GitHub 依赖

### 运行桌面版（最快验证）

```bash
cd wuji-compose
./gradlew :composeApp:run                    # Linux / macOS
# Windows PowerShell: .\gradlew.bat :composeApp:run
```

### 构建 Android APK

```bash
cd wuji-compose
./gradlew :composeApp:assembleDebug          # 输出:
# composeApp/build/outputs/apk/debug/composeApp-debug.apk
./gradlew :composeApp:assembleRelease        # release 包（需配置签名才能安装）
```

> 本地首次构建若遇到 Gradle wrapper 下载超时，请参考「常见问题」章节切换国内镜像或使用离线分发包。

### 构建桌面安装包

```bash
cd wuji-compose
./gradlew :composeApp:packageDeb        # Linux → composeApp/build/compose/binaries/main/deb/*.deb
./gradlew :composeApp:packageMsi        # Windows (需 WiX Toolset)
./gradlew :composeApp:packageDmg        # macOS
```

---

## 📦 远程推送 → GitHub Actions 自动生成分平台安装包

仓库配置 `.github/workflows/build-release.yml`，**无需本地构建，推送代码即可云端打包**。

### 触发方式

| 触发方式 | 结果 |
|----------|------|
| **`push` 到 `main` 分支**（PR 合并 / 直接 push） | 自动构建 APK + Linux deb + Windows msi + macOS dmg → 保存为 **Actions Artifacts**，保留 30 天 |
| **`push` tag `v*`**（如 `v0.0.02`） | 同上 + 自动创建 **GitHub Release** 并附加所有安装包 |
| **Actions 手动 `Run workflow`** | 选择任意分支手动构建，等同于 push main 效果 |

### Artifact 命名规范

- Android：`wuji-android-apk-0.0.02-{短SHA}`（内含 `wuji-v0.0.02-xxxxxxx-release.apk` & `wuji-v0.0.02-xxxxxxx-debug.apk`）
- 桌面：`wuji-desktop-linux` / `wuji-desktop-windows` / `wuji-desktop-macos`

### 获取构建产物

1. 仓库主页 → **Actions** Tab → 选择最新一次 `Build & Release` run
2. 进入 Summary，底部 **Artifacts** 区域点击下载
3. Tag 发布的安装包则访问 **Releases** 页面下载（永久留存）

---

## 🌿 Git 版本管理（GitHub-Flow 强制约束）

### 工作流

```
    main (受保护,禁止直接push)
     │
     ├── feature/模块-功能名        ← 新功能
     ├── fix/模块-bug描述           ← 缺陷修复
     ├── hotfix/线上紧急问题         ← 线上紧急
     ├── refactor/模块-重构说明      ← 代码重构
     ├── docs/文档更新               ← README 等文档
     ├── perf/性能优化               ← 性能优化
     ├── test/测试补充               ← 单测 UI 测试
     └── chore/构建-依赖-配置        ← 构建/依赖/CI
          ↓
       创建 PR (Squash and merge)
          ↓
        删除临时分支
```

### Commit Message 规范（Conventional Commits）

格式：`<type>[可选(scope)]: <简短描述>`

```
feat(user): 新增登录验证码接口
fix(order): 修复订单金额空指针报错
docs(readme): 更新 README 架构图与构建指南
chore(ci): push main 触发 APK 构建 + tag 发布 Release
perf(list): 优化列表 RecyclerView 缓存池大小
style: 统一 ktlint 格式化（不改变业务逻辑）
refactor(source): 拆分 Extension 多类型为独立文件
```

type 集合：`feat / fix / docs / style / refactor / perf / test / build / ci / chore`

### 每次提交流程（强制步骤）

1. **同步基线**：`git checkout main && git pull origin main` → 回到工作分支 `git merge main` → 解决冲突
2. **文件检查**：`git status` / `git diff --stat` → 绝对排除 `local.properties`、`build/`、`.gradle/`、`.idea/`、`*.log`、`.env*`
3. **暂存变更**：`git add <具体业务文件>`（禁止 `git add .`）
4. **本地提交**：一个 commit 只做一件事，遵循 Conventional Commits
5. **本地校验**：`git diff HEAD~1..HEAD` 确认内容
6. **推送远程**：`git push -u origin <分支名>`
7. **创建 PR**：GitHub 页面创建 PR（main ← 你的分支），按模板填写，关联 Issue `Closes #xxx`
8. **评审合并**：Squash and merge 到 main → **自动触发 CI 构建安装包**
9. **清理分支**：合并后删除本地 `git branch -D <分支>` + 远程 `git push origin --delete <分支>`

> **禁止行为**：不要直接 push main；不要无意义 commit message（如 `update code`、`fix bug`）；不要提交密钥、Token、本地配置文件。

---

## 🔑 远程推送：使用 Personal Access Token

GitHub 已弃用密码推送，需使用 **Personal Access Token (classic / fine-grained)**。推荐将其保存到 Git Credential，避免每次输入。

### 方式一：一次性在 remote URL 中嵌入（不推荐长期，仅临时）

```bash
# 仅示例，不要写死 Token 到文件或脚本
git remote set-url origin https://<YOUR_GITHUB_USERNAME>:<YOUR_PAT>@github.com/<OWNER>/<REPO>.git
git push -u origin <branch>
```

### 方式二：Git Credential Store（推荐）

```bash
# Linux / macOS
git config --global credential.helper store   # 或用 cache --timeout=3600
# Windows: git config --global credential.helper manager-core

# 首次推送时 Username 填 GitHub 用户名，Password 填 PAT
git push -u origin <branch>
```

> ⚠️ **安全强制准则**：Token、密钥、密码绝对禁止写入仓库文件（README、CI yaml、脚本、配置）；禁止打印 Token 到日志。Token 泄露请立即到 GitHub → Settings → Developer settings → Personal access tokens 撤销。

---

## 🔖 发布正式版（v0.0.02 流程）

```bash
# 1) 确保 main 已合并最新代码
git checkout main && git pull origin main

# 2) 打 tag 并推送（触发 Release 工作流）
git tag v0.0.02
git push origin v0.0.02
```

Actions 会完成：构建 APK + 三平台桌面包 → 创建 Release 并附带全部安装包 → 仓库 **Releases** 页可见。

> 下次发版：`versionCode = 3`，`versionName = 0.0.03`，tag `v0.0.03`（versionCode 必须单调递增，否则无法覆盖安装）。

---

## ❓ 常见问题

### 1. Gradle wrapper 分发包下载超时

症状：`Could not HEAD ... gradle-*-bin.zip Connection timed out`（参考经验 `#972788`）。

修复：

```bash
# 查看当前 wrapper URL
cat wuji-compose/gradle/wrapper/gradle-wrapper.properties

# ① 将 distributionUrl 替换为镜像（保持版本一致）
# distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-<VERSION>-bin.zip
# 或 https://mirrors.aliyun.com/macports/distfiles/gradle/gradle-<VERSION>-bin.zip

# ② 离线方案：手动下载 gradle-<VERSION>-bin.zip，放入
#    ~/.gradle/wrapper/dists/<wrapper-name>/<随机hash>/
#    目录（即 wrapper 自动创建的散列子目录），不解压
```

### 2. Android release APK 安装失败

原因：未配置签名。解决：
- 日常测试直接安装 **debug APK**（自带 debug.keystore 签名）
- 正式发布：在 `build.gradle.kts` `android.signingConfigs` 配置 release keystore，并把 `KEYSTORE_PASS`、`KEY_ALIAS`、`KEY_PASS` 存到 `local.properties` 或 GitHub Secrets，**禁止写入仓库**。

### 3. Windows 桌面包构建失败（允许失败）

CI 已设置 `continue-on-error: windows-latest`，不阻断 APK Release。若需要可在本地 Windows 机器安装 **WiX Toolset 3.x** 后执行 `.\gradlew.bat :composeApp:packageMsi`。

---

## 📜 许可证

GPL-3.0。
