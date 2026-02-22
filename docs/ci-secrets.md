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
