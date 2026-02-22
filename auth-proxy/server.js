import dotenv from 'dotenv'
import express from 'express'

dotenv.config()

const app = express()
const port = Number(process.env.PORT || 8787)
const defaultMobileCallbackUri = 'githubmobile://auth'

const githubTokenUrl = 'https://github.com/login/oauth/access_token'

app.set('trust proxy', true)
app.use(express.json({ limit: '256kb' }))
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*')
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization')
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
  if (req.method === 'OPTIONS') {
    res.status(204).end()
    return
  }
  next()
})

function getEnv(name) {
  return process.env[name]?.trim() || ''
}

function getClientConfig() {
  return {
    clientId: getEnv('GH_OAUTH_CLIENT_ID'),
    clientSecret: getEnv('GH_OAUTH_CLIENT_SECRET'),
  }
}

function getAllowedRedirectUris() {
  const raw = getEnv('OAUTH_REDIRECT_URI_ALLOWLIST')
  if (!raw) {
    return []
  }
  return raw
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function getMobileAppCallbackUri() {
  return getEnv('MOBILE_APP_CALLBACK_URI') || defaultMobileCallbackUri
}

function getRequestOrigin(req) {
  const forwardedProto = req.get('x-forwarded-proto')?.split(',')[0]?.trim()
  const forwardedHost = req.get('x-forwarded-host')?.split(',')[0]?.trim()
  const proto = forwardedProto || req.protocol
  const host = forwardedHost || req.get('host')
  return host ? `${proto}://${host}` : ''
}

function getProxyCallbackUri(req) {
  const origin = getRequestOrigin(req)
  if (!origin) {
    return ''
  }
  return new URL('/oauth/github/callback', origin).toString()
}

function getAllowedRedirectUriSet(req) {
  const allowed = new Set(getAllowedRedirectUris())
  const mobileCallback = getMobileAppCallbackUri()
  if (mobileCallback) {
    allowed.add(mobileCallback)
  }
  const proxyCallback = getProxyCallbackUri(req)
  if (proxyCallback) {
    allowed.add(proxyCallback)
  }
  return allowed
}

function getQueryValue(value) {
  if (Array.isArray(value)) {
    return value[0] || ''
  }
  return typeof value === 'string' ? value : ''
}

function buildCallbackUri(baseUri, query) {
  const callback = new URL(baseUri)
  const keys = ['code', 'state', 'error', 'error_description']
  keys.forEach((key) => {
    const value = getQueryValue(query[key])
    if (value) {
      callback.searchParams.set(key, value)
    }
  })
  return callback.toString()
}

function escapeHtml(value) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

function requireServerConfig(res) {
  const { clientId, clientSecret } = getClientConfig()
  if (!clientId || !clientSecret) {
    res.status(500).json({
      error: 'server_misconfigured',
      error_description: 'GH_OAUTH_CLIENT_ID 或 GH_OAUTH_CLIENT_SECRET 未配置',
    })
    return null
  }
  return { clientId, clientSecret }
}

async function requestGitHubToken(params) {
  const response = await fetch(githubTokenUrl, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/x-www-form-urlencoded',
      'User-Agent': 'github-mobile-auth-proxy',
    },
    body: new URLSearchParams(params),
  })

  const payload = await response.json().catch(() => ({}))
  return { response, payload }
}

function normalizeTokenResponse(payload) {
  return {
    access_token: payload.access_token,
    token_type: payload.token_type || 'bearer',
    scope: payload.scope || '',
    refresh_token: payload.refresh_token || null,
    expires_in: payload.expires_in ?? null,
    refresh_token_expires_in: payload.refresh_token_expires_in ?? null,
  }
}

app.get('/health', (_req, res) => {
  const { clientId, clientSecret } = getClientConfig()
  res.json({
    ok: true,
    provider: 'github-oauth',
    hasClientId: Boolean(clientId),
    hasClientSecret: Boolean(clientSecret),
    allowedRedirectUris: getAllowedRedirectUris(),
    mobileAppCallbackUri: getMobileAppCallbackUri(),
    oauthCallbackPath: '/oauth/github/callback',
    now: new Date().toISOString(),
  })
})

app.get('/oauth/github/callback', (req, res) => {
  const mobileCallbackUri = getMobileAppCallbackUri()

  let callbackUri = ''
  try {
    callbackUri = buildCallbackUri(mobileCallbackUri, req.query || {})
  } catch (_error) {
    res.status(500).send('Invalid MOBILE_APP_CALLBACK_URI configuration')
    return
  }

  const escapedUri = escapeHtml(callbackUri)
  const encodedUri = JSON.stringify(callbackUri)

  res.status(200).send(`<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>返回 App 登录</title>
  <style>
    body { font-family: system-ui, -apple-system, sans-serif; padding: 24px; line-height: 1.6; }
    a { color: #0969da; word-break: break-all; }
  </style>
</head>
<body>
  <h2>正在返回应用完成登录...</h2>
  <p>若没有自动跳转，请点击：</p>
  <p><a href="${escapedUri}">继续回到 GitHub Mobile</a></p>
  <script>
    const target = ${encodedUri};
    window.location.replace(target);
    setTimeout(() => { window.location.href = target; }, 1200);
  </script>
</body>
</html>`)
})

app.post('/oauth/github/exchange', async (req, res) => {
  const cfg = requireServerConfig(res)
  if (!cfg) return

  const { code, code_verifier: codeVerifier, redirect_uri: redirectUri } = req.body || {}

  if (!code || !codeVerifier || !redirectUri) {
    res.status(400).json({
      error: 'invalid_request',
      error_description: 'code/code_verifier/redirect_uri 均为必填',
    })
    return
  }

  const allowSet = getAllowedRedirectUriSet(req)
  if (allowSet.size > 0 && !allowSet.has(redirectUri)) {
    res.status(400).json({
      error: 'invalid_request',
      error_description: `redirect_uri 不在允许列表: ${redirectUri}`,
      allowed_redirect_uris: Array.from(allowSet),
    })
    return
  }

  try {
    const { payload } = await requestGitHubToken({
      client_id: cfg.clientId,
      client_secret: cfg.clientSecret,
      code,
      code_verifier: codeVerifier,
      redirect_uri: redirectUri,
    })

    if (!payload.access_token) {
      res.status(400).json({
        error: payload.error || 'exchange_failed',
        error_description: payload.error_description || 'GitHub 未返回 access_token',
      })
      return
    }

    res.status(200).json(normalizeTokenResponse(payload))
  } catch (error) {
    res.status(502).json({
      error: 'upstream_error',
      error_description: error instanceof Error ? error.message : 'Unknown upstream error',
    })
  }
})

app.post('/oauth/github/refresh', async (req, res) => {
  const cfg = requireServerConfig(res)
  if (!cfg) return

  const { refresh_token: refreshToken } = req.body || {}

  if (!refreshToken) {
    res.status(400).json({
      error: 'invalid_request',
      error_description: 'refresh_token 为必填',
    })
    return
  }

  try {
    const { payload } = await requestGitHubToken({
      client_id: cfg.clientId,
      client_secret: cfg.clientSecret,
      grant_type: 'refresh_token',
      refresh_token: refreshToken,
    })

    if (!payload.access_token) {
      res.status(400).json({
        error: payload.error || 'refresh_failed',
        error_description: payload.error_description || 'GitHub 未返回 access_token',
      })
      return
    }

    res.status(200).json(normalizeTokenResponse(payload))
  } catch (error) {
    res.status(502).json({
      error: 'upstream_error',
      error_description: error instanceof Error ? error.message : 'Unknown upstream error',
    })
  }
})

app.listen(port, () => {
  console.log(`[auth-proxy] listening on :${port}`)
})
