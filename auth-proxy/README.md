# Auth Proxy

给 Android 客户端提供 GitHub OAuth 授权码换 token 的后端。

## 接口

- `GET /health`
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

## 给 Android 用的地址

如果部署地址是 `https://proxy.example.com`，
则仓库 Secret `GH_AUTH_PROXY_BASE_URL` 填：

`https://proxy.example.com`

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
- `OAUTH_REDIRECT_URI_ALLOWLIST`（默认 `githubmobile://auth`）
