# Auth Proxy

给 Android 客户端提供 GitHub OAuth 授权码换 token 的后端。

## 接口

- `GET /health`
- `GET /oauth/github/callback`（浏览器授权完成后桥接回 App）
- `POST /oauth/github/exchange`
- `POST /oauth/github/refresh`

## 本地运行

```bash
cd auth-proxy
cp .env.example .env
# 编辑 .env 填入 GH_OAUTH_CLIENT_ID / GH_OAUTH_CLIENT_SECRET
npm install
npm start
```

启动后测试：

```bash
curl http://127.0.0.1:8787/health
```

回调桥接测试：

```bash
open "http://127.0.0.1:8787/oauth/github/callback?code=demo&state=demo"
```

会尝试跳转到 `MOBILE_APP_CALLBACK_URI`（默认 `githubmobile://auth`）。

## 给 Android 用的地址

如果部署地址是 `https://proxy.example.com`，
则仓库 Secret `GH_AUTH_PROXY_BASE_URL` 填：

`https://proxy.example.com`

并在 GitHub OAuth App 中把 Authorization callback URL 设置为：

`https://proxy.example.com/oauth/github/callback`

客户端会自动调用：

- `https://proxy.example.com/oauth/github/exchange`
- `https://proxy.example.com/oauth/github/refresh`

## 部署建议

- Railway / Render / Fly.io 任意一个均可
- 运行命令：`npm start`
- Node 版本：`>=18`

部署时环境变量必须配置：

- `GH_OAUTH_CLIENT_ID`
- `GH_OAUTH_CLIENT_SECRET`
- `MOBILE_APP_CALLBACK_URI`（默认 `githubmobile://auth`，一般不改）

可选环境变量：

- `OAUTH_REDIRECT_URI_ALLOWLIST`：限制允许 exchange 的 `redirect_uri`（逗号分隔）。
  - 留空时：自动允许 `MOBILE_APP_CALLBACK_URI` 与当前服务 `https://<your-host>/oauth/github/callback`。
