# GitHub Mobile Android

一款基于 Kotlin + Jetpack Compose 的 Android GitHub 客户端（V1），支持：

- OAuth App + PKCE 登录流程（通过后端代理交换 token）
- 仓库列表、通知、搜索、个人信息基础能力
- Google Translate API 文本翻译（含本地缓存）
- “网页回退打开”入口（可跳转 GitHub 网页处理未覆盖能力）
- GitHub Actions 自动构建 Debug/Release APK

> 你提到的 `apo` 已按 `APK` 产物实现。

## 技术栈

- Kotlin 2.0 + Jetpack Compose
- Hilt DI
- Retrofit + OkHttp + Kotlinx Serialization
- Room + DataStore + EncryptedSharedPreferences

## 项目结构

- `app/src/main/java/com/antihub/mobile/data/auth`: OAuth PKCE 与会话管理
- `app/src/main/java/com/antihub/mobile/data/remote`: GitHub/Auth/翻译 API 定义
- `app/src/main/java/com/antihub/mobile/data/repository`: 仓储实现
- `app/src/main/java/com/antihub/mobile/ui`: Compose UI 与 ViewModel
- `.github/workflows`: APK CI/CD 工作流

## 本地开发

### 1. 先决条件

- JDK 17
- Android SDK（建议 API 35）
- 可用的 GitHub OAuth App 与 token 交换代理服务
- 可用的 Google Translate API key

### 2. 设置环境变量

在执行 Gradle 命令前导出（示例）：

```sh
export GH_OAUTH_CLIENT_ID="your_client_id"
export GH_OAUTH_REDIRECT_SCHEME="githubmobile"
export GH_OAUTH_REDIRECT_HOST="auth"
export GH_AUTH_PROXY_BASE_URL="https://your-auth-proxy.example.com"
export TRANSLATION_API_BASE_URL="https://translation.googleapis.com"
export GOOGLE_TRANSLATE_API_KEY="your_google_api_key"
```

OAuth 相关至少需要确保：

- `GH_OAUTH_CLIENT_ID` 不能为空
- GitHub OAuth App 的 Authorization callback URL 与客户端一致（默认）：`githubmobile://auth`

### 3. 构建 Debug APK

```sh
./gradlew assembleDebug
```

输出：`app/build/outputs/apk/debug/app-debug.apk`

## OAuth 说明

移动端不应内置 OAuth Client Secret。
当前实现采用 `OAuth + PKCE` 发起授权，并将 `code` 发送到后端代理 `GH_AUTH_PROXY_BASE_URL` 进行安全换 token。

如果你看到授权链接中出现：

- `client_id=`
- `redirect_uri=%3A%2F%2F`

说明构建时 OAuth 环境变量为空，应用会拒绝发起授权并提示配置错误。

后端至少需要实现：

- `POST /oauth/github/exchange`
- `POST /oauth/github/refresh`

请求/响应结构见 `app/src/main/java/com/antihub/mobile/data/remote/AuthApiService.kt`。

## 翻译说明

- 默认使用 Google Translate API
- 翻译结果缓存到 Room（`translation_cache`）
- 若 API key 缺失，会返回可见错误并不影响 GitHub 主流程

## GitHub Actions 产 APK

- `android-ci.yml`: PR/主分支构建 debug APK
- `android-release.yml`: `v*` tag 构建并发布 release APK

需要配置的 secrets 见：`docs/ci-secrets.md`

## 风险与后续

- 目前 UI 为首版骨架，未覆盖 Issues/PR 详情编辑与 WebView 回退页
- 生产环境建议将翻译能力改为后端代理，避免 API key 暴露在客户端
