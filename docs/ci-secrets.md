# CI Secrets 清单

## 必需（Debug/Release 都建议）

- `GH_OAUTH_CLIENT_ID`
- `GH_OAUTH_REDIRECT_SCHEME`
- `GH_OAUTH_REDIRECT_HOST`
- `GH_AUTH_PROXY_BASE_URL`
- `TRANSLATION_API_BASE_URL`
- `GOOGLE_TRANSLATE_API_KEY`

## Release 必需

- `ANDROID_KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

## 说明

- `ANDROID_KEYSTORE_BASE64`: 使用 `base64` 编码后的 keystore 文件内容
- Release workflow 会在运行时解码为 `${{ github.workspace }}/release.keystore`
- `GH_OAUTH_CLIENT_ID` 必须设置为 GitHub OAuth App 的 Client ID，不能为空
- 默认回调 URI 为 `githubmobile://auth`，需在 GitHub OAuth App 配置的 callback URL 中保持一致
- `GH_AUTH_PROXY_BASE_URL` 必须是可访问的后端服务地址，不能使用示例值 `https://example.com`
